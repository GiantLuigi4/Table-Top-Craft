package andrews.table_top_craft.util.lod;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector3f;
import net.minecraft.world.phys.Vec2;

import java.util.ArrayList;
import java.util.List;

public class Triangle {
	Vector3f vertex0;
	Vector3f vertex1;
	Vector3f vertex2;
	Vector3f normal0;
	Vector3f normal1;
	Vector3f normal2;
	Vec2 tex0;
	Vec2 tex1;
	Vec2 tex2;
	
	public Triangle(Vector3f vertex0, Vector3f vertex1, Vector3f vertex2, Vector3f normal0, Vector3f normal1, Vector3f normal2, Vec2 tex0, Vec2 tex1, Vec2 tex2) {
		this.vertex0 = vertex0;
		this.vertex1 = vertex1;
		this.vertex2 = vertex2;
		this.normal0 = normal0;
		this.normal1 = normal1;
		this.normal2 = normal2;
		this.tex0 = tex0;
		this.tex1 = tex1;
		this.tex2 = tex2;
	}
	
	public List<Triangle> filterNeighbors(Triangle[] triangles) {
		ArrayList<Triangle> neighbors = new ArrayList<>();
		for (Triangle triangle : triangles) {
			if (triangle.hasMatchingCorner(this)) {
				neighbors.add(triangle);
			}
		}
		return neighbors;
	}
	
	public boolean hasMatchingCorner(Triangle other) {
		if (other.equals(this)) return false;
		Vector3f[] vecs0 = new Vector3f[]{vertex0, vertex1, vertex2};
		for (Vector3f vector3d : vecs0) {
			if (vector3d.equals(other.vertex0)) return true;
			if (vector3d.equals(other.vertex1)) return true;
			if (vector3d.equals(other.vertex2)) return true;
		}
		return false;
	}
	
	public boolean containsPoint(Vector3f vertex1) {
		return this.vertex0.equals(vertex1) || this.vertex1.equals(vertex1) || this.vertex2.equals(vertex1);
	}
}
