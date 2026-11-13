using System;
// Synthetic timestamps only: no COM activation or audio recording.
static class LoopbackAudioRegression {
 static int Main(){LoopbackClock clock=new LoopbackClock();double first=100000;for(uint i=0;i<6000;i++){double stamped=clock.Stamp(first+i*10+(i==0?0:i%2==0?4:-3),i*480,480,0,48000);if(Math.Abs(stamped-(first+i*10))>0.001)throw new Exception("Device sample clock corrupted by packet timestamp jitter");}
  double gap=clock.Stamp(first+60100,6000*480,480,1,48000);if(gap!=first+60100)throw new Exception("Real device discontinuity concealed");Console.WriteLine("PASS: 60 seconds of sample-exact Windows loopback packets under timestamp jitter; real discontinuities preserved; no device opened");return 0;}
}
