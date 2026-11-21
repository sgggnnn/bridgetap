#version 120
uniform sampler2D image;
uniform float opacity;
uniform vec2 panelOrigin;
uniform vec2 panelSize;
uniform float radius;
varying vec2 uv;
void main(){
    float coverage=1.0;
    if(panelSize.x>0.0){
        vec2 q=abs(gl_FragCoord.xy-panelOrigin-panelSize*.5)-(panelSize*.5-vec2(radius));
        float d=length(max(q,vec2(0.0)))+min(max(q.x,q.y),0.0)-radius;
        coverage=1.0-smoothstep(-.5,.5,d);
    }
    gl_FragColor=vec4(texture2D(image,uv).rgb,opacity*coverage);
}
