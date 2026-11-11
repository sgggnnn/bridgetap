package com.bridger.momentum;

import java.lang.invoke.*;
import java.lang.reflect.Method;

/** LWJGL 2 uses GLSync objects; LWJGL 3 / client adapters use long handles. */
public final class ClipGpuSync {
    private final MethodHandle create,wait,delete;
    public ClipGpuSync(Class<?> api){
        try{
            Method factory=api.getMethod("glFenceSync",int.class,int.class);Class<?> token=factory.getReturnType();MethodHandles.Lookup lookup=MethodHandles.publicLookup();
            create=lookup.unreflect(factory).asType(MethodType.methodType(Object.class,int.class,int.class));
            wait=lookup.unreflect(api.getMethod("glClientWaitSync",token,int.class,long.class)).asType(MethodType.methodType(int.class,Object.class,int.class,long.class));
            delete=lookup.unreflect(api.getMethod("glDeleteSync",token)).asType(MethodType.methodType(void.class,Object.class));
        }catch(ReflectiveOperationException ex){throw new IllegalStateException("Unsupported GPU fence API: "+api.getName(),ex);}
    }
    public Object create(){try{return (Object)create.invokeExact(0x9117,0);}catch(Throwable error){throw failed(error);}}
    public int poll(Object token){try{return (int)wait.invokeExact(token,0,0L);}catch(Throwable error){throw failed(error);}}
    public void delete(Object token){try{delete.invokeExact(token);}catch(Throwable error){throw failed(error);}}
    private static RuntimeException failed(Throwable error){if(error instanceof Error)throw (Error)error;if(error instanceof RuntimeException)return (RuntimeException)error;return new IllegalStateException("GPU fence operation failed",error);}
}
