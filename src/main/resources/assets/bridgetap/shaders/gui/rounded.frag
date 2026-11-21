#version 120
uniform vec2 size;
uniform float radius;
uniform float stroke;
uniform float reflection;
uniform vec4 fill;
uniform vec4 border;
varying vec2 uv;
float roundBox(vec2 p,vec2 halfSize,float r){vec2 q=abs(p)-halfSize+vec2(r);return length(max(q,0.0))+min(max(q.x,q.y),0.0)-r;}
void main(){
 float d=roundBox((uv-0.5)*size,size*0.5,radius);
 float aa=max(fwidth(d)*0.75,0.001);
 float outer=1.0-smoothstep(-aa,aa,d);
 float inner=1.0-smoothstep(-aa,aa,d+stroke*max(fwidth(d),0.001));
 float edge=max(0.0,outer-inner)*border.a;
 float body=inner*fill.a;
 float alpha=body+edge;
 vec3 rgb=(fill.rgb*body+border.rgb*edge)/max(alpha,0.0001);
 rgb+=vec3(reflection*pow(1.0-uv.y,3.0));
 gl_FragColor=vec4(rgb,alpha);
}
