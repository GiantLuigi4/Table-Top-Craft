package andrews.table_top_craft.util.lod;

import andrews.table_top_craft.util.obj.ObjModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class LODMesh extends ObjModel {
	HashMap<LODLevel, ObjModel> modelsLOD = new HashMap<>();
	
	public LODMesh(Vector3f[] v, Vec2[] vt, Vector3f[] vn, Face[] faces) {
		super(v, vt, vn, faces);
		
		Random rng = new Random(2397439247L);
		
		Triangle[] triangles = new Triangle[faces.length];
		
		float maxY = 0;
		
		for (int i = 0; i < faces.length; i++) {
			Face face = faces[i];
			
			Vector3f v1 = v[face.v1 - 1];
			Vector3f v2 = v[face.v2 - 1];
			Vector3f v3 = v[face.v3 - 1];
			maxY = Math.max(
					maxY, Math.max(
							v1.y(), Math.max(
									v2.y(),
									v3.y()
							)));
			
			Vec2 vt1 = vt[face.vt1 - 1];
			Vec2 vt2 = vt[face.vt2 - 1];
			Vec2 vt3 = vt[face.vt3 - 1];
			
			Vector3f vn1 = vn[face.vn1 - 1];
			Vector3f vn2 = vn[face.vn2 - 1];
			Vector3f vn3 = vn[face.vn3 - 1];
			
			triangles[i] = new Triangle(
					v1, v2, v3,
					vn1, vn2, vn3,
					vt1, vt2, vt3
			);
		}
		float threshold = maxY / 3f;
		
		for (LODLevel value : LODLevel.values()) {
			float chance = 1 - value.loss;
			
			ArrayList<Triangle> vertices = new ArrayList<>();
			HashMap<Triangle, Triangle> collapsed = new HashMap<>();
			
			for (int i = 0; i < triangles.length; i++) {
				Triangle tri = triangles[i];
				if (rng.nextDouble() >= chance) {
					if (!collapsed.containsKey(tri)) {
						List<Triangle> tris = tri.filterNeighbors(triangles);
						for (Triangle triangle : tris) {
							Triangle other = collapsed.getOrDefault(triangle, triangle);
							
							if (other != null) {
								Triangle collapsedTri = new Triangle(
										tri.containsPoint(other.vertex0) ? other.vertex0 : lerp(0.75, new Vector3f(0, (other.vertex0.y() < threshold ? 0 : maxY / 2), 0), other.vertex0),
										tri.containsPoint(other.vertex1) ? other.vertex1 : lerp(0.75, new Vector3f(0, (other.vertex1.y() < threshold ? 0 : maxY / 2), 0), other.vertex1),
										tri.containsPoint(other.vertex2) ? other.vertex2 : lerp(0.75, new Vector3f(0, (other.vertex2.y() < threshold ? 0 : maxY / 2), 0), other.vertex2),
										other.normal0, other.normal1, other.normal2,
										other.tex0, other.tex1, other.tex2
								);
								collapsed.put(other, collapsedTri);
								if (!vertices.contains(other)) {
									vertices.add(collapsedTri);
								}
							}
						}
						collapsed.put(tri, null);
					}
				} else {
					Triangle old;
					if ((old = collapsed.put(tri, tri)) == null) {
						vertices.add(tri);
					} else {
						collapsed.put(tri, old);
					}
				}
			}
			
			Vector3f[] lodVectors = new Vector3f[vertices.size() * 3];
			Vector3f[] lodNormals = new Vector3f[vertices.size() * 3];
			Vec2[] lodTexture = new Vec2[vertices.size() * 3];
			Face[] faces1 = new Face[vertices.size()];
			for (int i = 0; i < vertices.size(); i++) {
				Triangle tri = vertices.get(i);
				lodVectors[i * 3] = tri.vertex0;
				lodNormals[i * 3] = tri.normal0;
				lodTexture[i * 3] = tri.tex0;
				lodVectors[i * 3 + 1] = tri.vertex1;
				lodNormals[i * 3 + 1] = tri.normal1;
				lodTexture[i * 3 + 1] = tri.tex1;
				lodVectors[i * 3 + 2] = tri.vertex2;
				lodNormals[i * 3 + 2] = tri.normal2;
				lodTexture[i * 3 + 2] = tri.tex2;
				
				int x3 = i * 3;
				faces1[i] = new Face(
						(x3 + 1) + "/" + (x3 + 1) + "/" + (x3 + 1),
						(x3 + 2) + "/" + (x3 + 2) + "/" + (x3 + 2),
						(x3 + 3) + "/" + (x3 + 3) + "/" + (x3 + 3)
				);
			}
			
			// TODO: mesh simplification to save on memory
			modelsLOD.put(value, new ObjModel(lodVectors, lodTexture, lodNormals, faces1));
		}
	}
	
	private static Vector3f lerp(double pct, Vector3f src, Vector3f dst) {
		return new Vector3f(
				(float) Mth.lerp(pct, src.x(), dst.x()),
				(float) Mth.lerp(pct, src.y(), dst.y()),
				(float) Mth.lerp(pct, src.z(), dst.z())
		);
	}
	
	public static LODMesh of(ObjModel loadModel) {
		return new LODMesh(loadModel.v, loadModel.vt, loadModel.vn, loadModel.faces);
	}
	
	public void render(PoseStack stack, VertexConsumer buffer, boolean quads, LODLevel level) {
//		if (level == LODLevel.MAX) super.render(stack, buffer, quads);
//		else modelsLOD.get(level).render(stack, buffer, quads);
		modelsLOD.get(level).render(stack, buffer, quads);
	}
}
