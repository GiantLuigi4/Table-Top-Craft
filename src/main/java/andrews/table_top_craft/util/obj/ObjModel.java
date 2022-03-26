package andrews.table_top_craft.util.obj;

import andrews.table_top_craft.util.Reference;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@OnlyIn(Dist.CLIENT)
public class ObjModel
{
    private final Vector3f[] v;
    private final Vec2[] vt;
    private final Vector3f[] vn;
    private final Face[] faces;
    
    private ObjModel(Vector3f[] v, Vec2[] vt, Vector3f[] vn, Face[] faces)
    {
        this.v = v;
        this.vt = vt;
        this.vn = vn;
        this.faces = faces;
    }
    
    public void render(PoseStack stack, BufferBuilder buffer)
    {    	
        try
        {
            for(Face face : faces)
            {
                Vector3f v1 = v[face.v1 - 1];
                Vector3f v2 = v[face.v2 - 1];
                Vector3f v3 = v[face.v3 - 1];

                Vec2 vt1 = vt[face.vt1 - 1];
                Vec2 vt2 = vt[face.vt2 - 1];
                Vec2 vt3 = vt[face.vt3 - 1];

                Vector3f vn1 = vn[face.vn1 - 1];
                Vector3f vn2 = vn[face.vn2 - 1];
                Vector3f vn3 = vn[face.vn3 - 1];

                addVertex(stack, buffer, v1.x(), v1.y(), v1.z(), vt1.x, -vt1.y, vn1.x(), vn1.y(), vn1.z());
                addVertex(stack, buffer, v2.x(), v2.y(), v2.z(), vt2.x, -vt2.y, vn2.x(), vn2.y(), vn2.z());
                addVertex(stack, buffer, v3.x(), v3.y(), v3.z(), vt3.x, -vt3.y, vn3.x(), vn3.y(), vn3.z());
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    private void addVertex(PoseStack stack, BufferBuilder builder, float x, float y, float z, float u, float v, float nx, float ny, float nz)
    {
    	pos(builder, stack.last().pose(), x, y, z)
    	.color(1F, 1F, 1F, 1F)
    	.uv(u, v)
    	.uv2(0, 240); // These values are full brightness
    	normal(builder, stack.last().normal(), nx, ny, nz)
    	.endVertex();   
    }
    
	private static BufferBuilder pos(BufferBuilder bufferBuilder, Matrix4f matrix4f, float x, float y, float z)
	{
		// Calling 'bufferBuilder.pos(matrix4f, x, y, z)' allocates a Vector4f
		// To avoid allocating so many short-lived vectors we do the transform ourselves instead
		float w = 1.0F;
		float tx = matrix4f.m00 * x + matrix4f.m01 * y + matrix4f.m02 * z + matrix4f.m03 * w;
		float ty = matrix4f.m10 * x + matrix4f.m11 * y + matrix4f.m12 * z + matrix4f.m13 * w;
		float tz = matrix4f.m20 * x + matrix4f.m21 * y + matrix4f.m22 * z + matrix4f.m23 * w;
		
		return (BufferBuilder) bufferBuilder.vertex(tx, ty, tz);//TODO this was pos() make sure it works
	}
	
	private static BufferBuilder normal(BufferBuilder bufferBuilder, Matrix3f matrix3f, float x, float y, float z)
	{
		// Calling 'bufferBuilder.normal(matrix3f, x, y, z)' allocates a Vector3f
		// To avoid allocating so many short-lived vectors we do the transform ourselves instead
	    float nx = matrix3f.m00 * x + matrix3f.m01 * y + matrix3f.m02 * z;
	    float ny = matrix3f.m10 * x + matrix3f.m11 * y + matrix3f.m12 * z;
	    float nz = matrix3f.m20 * x + matrix3f.m21 * y + matrix3f.m22 * z;
	      
	    return (BufferBuilder) bufferBuilder.normal(nx, ny, nz);
	}
    
    public static ObjModel loadModel(ResourceLocation resourceLocation)
    {
        byte[] modelData = loadResource(resourceLocation);
        String modelString = new String(modelData);
        String[] modelLines = modelString.split("\\r?\\n");
        
        ArrayList<Vector3f> vList = new ArrayList<Vector3f>();
        ArrayList<Vec2> vtList = new ArrayList<Vec2>();
        ArrayList<Vector3f> vnList = new ArrayList<Vector3f>();
        ArrayList<Face> faceList = new ArrayList<Face>();

        for(String line : modelLines)
        {
            String[] lineSpit = line.split(" ");
            if (lineSpit[0].equals("v"))
            {
                vList.add(new Vector3f(Float.parseFloat(lineSpit[1]), Float.parseFloat(lineSpit[2]), Float.parseFloat(lineSpit[3])));
            }
            if (lineSpit[0].equals("vt"))
            {
                vtList.add(new Vec2(Float.parseFloat(lineSpit[1]), Float.parseFloat(lineSpit[2])));
            }
            if (lineSpit[0].equals("vn"))
            {
                vnList.add(new Vector3f(Float.parseFloat(lineSpit[1]), Float.parseFloat(lineSpit[2]), Float.parseFloat(lineSpit[3])));
            }
            if (lineSpit[0].equals("f"))
            {
                faceList.add(new Face(lineSpit[1], lineSpit[2], lineSpit[3]));
            }
        }
        
        Vector3f[] vArray = vList.toArray(new Vector3f[vList.size()]);
        Vec2[] vtArray = vtList.toArray(new Vec2[vtList.size()]);
        Vector3f[] vnArray = vnList.toArray(new Vector3f[vnList.size()]);
        Face[] faces = faceList.toArray(new Face[faceList.size()]);
        
        return new ObjModel(vArray, vtArray, vnArray, faces);
    }
    
    private static byte[] loadResource(ResourceLocation resourceLocation)
    {
        InputStream input = null;
        ByteArrayOutputStream output = null;
        try {
            input = ObjModel.class.getClassLoader().getResourceAsStream("assets/" + Reference.MODID + "/" + resourceLocation.getPath());
            if(input != null)
            {
                output = new ByteArrayOutputStream();
                IOUtils.copy(input, output);
                output.flush();
                return output.toByteArray();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
        return null;
    }
    
    private static class Face
    {
        // Vertex
        public int v1;
        public int v2;
        public int v3;
        // Texture
        public int vt1;
        public int vt2;
        public int vt3;
        // Normal
        public int vn1;
        public int vn2;
        public int vn3;
        
        // f v1/vt1/vn1 v2/vt2/vn2 v3/vt3/vn3
        
        public Face(String v1, String v2, String v3)
        {
            String[] s1 = v1.split("/");
            String[] s2 = v2.split("/");
            String[] s3 = v3.split("/");
            
            this.v1 = Integer.parseInt(s1[0]);
            this.vt1 = Integer.parseInt(s1[1]);
            this.vn1 = Integer.parseInt(s1[2]);
            
            this.v2 = Integer.parseInt(s2[0]);
            this.vt2 = Integer.parseInt(s2[1]);
            this.vn2 = Integer.parseInt(s2[2]);
            
            this.v3 = Integer.parseInt(s3[0]);
            this.vt3 = Integer.parseInt(s3[1]);
            this.vn3 = Integer.parseInt(s3[2]);
        }
    }
}