package andrews.table_top_craft.util.lod;

public enum LODLevel {
	MAX(0),
	L1(0.05f),
	L2(0.08f),
	L3(0.11f),
	L4(0.125f),
	L5(0.15f),
	L6(0.2f),
	L7(0.3f),
	;
	
	float loss;
	
	LODLevel(float loss) {
		this.loss = loss;
	}
}
