// Windows WASAPI loopback capture. No microphone and no visible helper window.
using System;
using System.IO;
using System.Text;
using System.Diagnostics;
using System.Collections.Generic;
using System.Collections.Concurrent;
using System.Globalization;
using System.Runtime.InteropServices;
using System.Threading;

[ComImport, Guid("BCDE0395-E52F-467C-8E3D-C4579291692E")] class DeviceEnumerator {}
[ComImport, Guid("A95664D2-9614-4F35-A746-DE8DB63617E6"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface IDevices {
    [PreserveSig] int EnumAudioEndpoints(int flow,int mask,out IntPtr devices);
    [PreserveSig] int GetDefaultAudioEndpoint(int flow,int role,out IDevice device);
}
[ComImport, Guid("D666063F-1587-4E43-81F1-B948E807363F"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface IDevice {
    [PreserveSig] int Activate(ref Guid iid,int context,IntPtr parameters,[MarshalAs(UnmanagedType.IUnknown)] out object obj);
}
[ComImport, Guid("1CB9AD4C-DBFA-4C32-B178-C2F568A703B2"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface IAudio {
    [PreserveSig] int Initialize(int mode,uint flags,long duration,long period,IntPtr format,IntPtr session);
    [PreserveSig] int GetBufferSize(out uint frames);
    [PreserveSig] int GetStreamLatency(out long latency);
    [PreserveSig] int GetCurrentPadding(out uint frames);
    [PreserveSig] int IsFormatSupported(int mode,IntPtr format,out IntPtr closest);
    [PreserveSig] int GetMixFormat(out IntPtr format);
    [PreserveSig] int GetDevicePeriod(out long normal,out long minimum);
    [PreserveSig] int Start();
    [PreserveSig] int Stop();
    [PreserveSig] int Reset();
    [PreserveSig] int SetEventHandle(IntPtr handle);
    [PreserveSig] int GetService(ref Guid iid,[MarshalAs(UnmanagedType.IUnknown)] out object obj);
}
[ComImport, Guid("C8ADBD64-E71E-48A0-A4DE-185C395CD317"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface ICapture {
    [PreserveSig] int GetBuffer(out IntPtr data,out uint frames,out uint flags,out ulong position,out ulong qpc);
    [PreserveSig] int ReleaseBuffer(uint frames);
    [PreserveSig] int GetNextPacketSize(out uint frames);
}
// Sample positions, rather than thread wake-up times, define contiguous audio.
internal sealed class LoopbackClock {
    bool havePosition;ulong nextPosition;double nextStart;
    public double Stamp(double timestamp,ulong position,uint frames,uint flags,int rate){
        if(havePosition&&(flags&1)==0&&position==nextPosition)timestamp=nextStart;
        nextPosition=position+frames;nextStart=timestamp+frames*1000.0/rate;havePosition=true;return timestamp;
    }
}
static class Loopback {
    class Packet { public double Start;public byte[] Data; }
    static readonly Queue<Packet> packets=new Queue<Packet>();
    static readonly ConcurrentQueue<string> commands=new ConcurrentQueue<string>();
    static readonly CultureInfo Inv=CultureInfo.InvariantCulture;
    static byte[] format;static int rate,align;static long bytes;
    static double Now {get{return (DateTime.UtcNow-new DateTime(1970,1,1)).TotalMilliseconds;}}
    static void Check(int code){Marshal.ThrowExceptionForHR(code);}
    static void Say(string text){Console.Out.WriteLine(text);Console.Out.Flush();}
    [MTAThread] static int Main(string[] args){
        IAudio audio=null;object devices=null,device=null,captureObj=null;IntPtr nativeFormat=IntPtr.Zero;
        try {
            devices=new DeviceEnumerator();IDevice endpoint;Check(((IDevices)devices).GetDefaultAudioEndpoint(0,0,out endpoint));device=endpoint;
            Guid iid=typeof(IAudio).GUID;object obj;Check(endpoint.Activate(ref iid,23,IntPtr.Zero,out obj));audio=(IAudio)obj;
            Check(audio.GetMixFormat(out nativeFormat));int size=18+(ushort)Marshal.ReadInt16(nativeFormat,16);
            if(size>128)throw new Exception("Unsupported output format");format=new byte[size];Marshal.Copy(nativeFormat,format,0,size);
            rate=BitConverter.ToInt32(format,4);align=BitConverter.ToUInt16(format,12);
            if(rate<8000||rate>192000||align<1||align>64)throw new Exception("Unsupported output format");
            Check(audio.Initialize(0,0x20000,10000000,0,nativeFormat,IntPtr.Zero));iid=typeof(ICapture).GUID;Check(audio.GetService(ref iid,out captureObj));ICapture capture=(ICapture)captureObj;
            double offset=Now-Stopwatch.GetTimestamp()*1000.0/Stopwatch.Frequency;
            Check(audio.Start());Say("READY");
            Thread input=new Thread(()=>{string line;while((line=Console.In.ReadLine())!=null)commands.Enqueue(line);commands.Enqueue("STOP");});input.IsBackground=true;input.Start();
            bool running=true;LoopbackClock clock=new LoopbackClock();
            while(running){
                uint count;Check(capture.GetNextPacketSize(out count));
                while(count>0){
                    IntPtr data;uint frames,flags;ulong pos,qpc;Check(capture.GetBuffer(out data,out frames,out flags,out pos,out qpc));
                    try {
                        byte[] pcm=new byte[checked((int)frames*align)];if((flags&2)==0)Marshal.Copy(data,pcm,0,pcm.Length);
                        double time=(flags&4)!=0?Now-frames*1000.0/rate:qpc/10000.0+offset;
                        // Adjacent device positions are adjacent samples, regardless of
                        // QPC conversion/polling jitter. Preserve real discontinuities.
                        time=clock.Stamp(time,pos,frames,flags,rate);
                        packets.Enqueue(new Packet{Start=time,Data=pcm});bytes+=pcm.Length;
                    } finally {Check(capture.ReleaseBuffer(frames));}
                    Check(capture.GetNextPacketSize(out count));
                }
                while(packets.Count>0&&(packets.Peek().Start<Now-65000||bytes>128L*1024*1024))bytes-=packets.Dequeue().Data.Length;
                string command;while(commands.TryDequeue(out command)){
                    if(command=="STOP"){running=false;break;}
                    string[] parts=command.Split(' ');if(parts.Length!=5||parts[0]!="SAVE")continue;
                    try{Save(double.Parse(parts[2],Inv),double.Parse(parts[3],Inv),Encoding.UTF8.GetString(Convert.FromBase64String(parts[4])));Say("SAVED "+parts[1]);}
                    catch(Exception ex){Say("ERROR "+parts[1]+" "+Convert.ToBase64String(Encoding.UTF8.GetBytes(ex.Message)));}
                }
                Thread.Sleep(5);
            }
            return 0;
        }catch(Exception ex){Say("FAILED "+Convert.ToBase64String(Encoding.UTF8.GetBytes(ex.Message)));return 1;}
        finally{if(audio!=null){try{audio.Stop();}catch{}Marshal.ReleaseComObject(audio);}if(captureObj!=null)Marshal.ReleaseComObject(captureObj);if(device!=null)Marshal.ReleaseComObject(device);if(devices!=null)Marshal.ReleaseComObject(devices);if(nativeFormat!=IntPtr.Zero)Marshal.FreeCoTaskMem(nativeFormat);}
    }
    static void Save(double start,double end,string file){
        if(double.IsNaN(start)||double.IsNaN(end)||end<=start||end-start>62000)throw new Exception("Invalid audio interval");
        int frames=checked((int)Math.Ceiling((end-start)*rate/1000));int length=checked(frames*align);if(length>128*1024*1024)throw new Exception("Audio snapshot too large");byte[] output=new byte[length];
        foreach(Packet p in packets){
            int offset=(int)Math.Round((p.Start-start)*rate/1000),source=Math.Max(0,-offset),target=Math.Max(0,offset),take=Math.Min(p.Data.Length/align-source,frames-target);
            if(take>0)Buffer.BlockCopy(p.Data,source*align,output,target*align,take*align);
        }
        using(BinaryWriter writer=new BinaryWriter(new FileStream(file,FileMode.CreateNew,FileAccess.Write))){
            writer.Write(Encoding.ASCII.GetBytes("RIFF"));writer.Write(20+format.Length+length);writer.Write(Encoding.ASCII.GetBytes("WAVEfmt "));writer.Write(format.Length);writer.Write(format);writer.Write(Encoding.ASCII.GetBytes("data"));writer.Write(length);writer.Write(output);
        }
    }
}
