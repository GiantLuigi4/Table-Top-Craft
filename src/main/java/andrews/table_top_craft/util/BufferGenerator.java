package andrews.table_top_craft.util;

import andrews.table_top_craft.game_logic.chess.pieces.BasePiece.PieceModelSet;
import andrews.table_top_craft.game_logic.chess.pieces.BasePiece.PieceType;
import andrews.table_top_craft.util.lod.LODLevel;
import andrews.table_top_craft.util.obj.models.ChessObjModel;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;

public class BufferGenerator
{
	// The texture path is just a dummy texture used as a placeholder
	public static final VertexFormat chessVertexFormat = TTCRenderTypes.getChessPieceSolid(new ResourceLocation(Reference.MODID, "textures/tile/chess/pieces.png")).format();
	// Initializes the models
	public static final ChessObjModel CHESS_PIECE_MODEL = new ChessObjModel();
	// The model buffers, used to render the VOBs
	public static final HashMap<LODLevel, HashMap<Pair<PieceType, PieceModelSet>, VertexBuffer>> BUFFERS = new HashMap<>();
	
	public static void setup()
	{
		BufferBuilder chessBuilder = new BufferBuilder(RenderType.TRANSIENT_BUFFER_SIZE);
		
		for (LODLevel level : LODLevel.values())
		{
			HashMap<Pair<PieceType, PieceModelSet>, VertexBuffer> BUFFERS = BufferGenerator.BUFFERS.computeIfAbsent(level, k -> new HashMap<>());
			
			for (PieceType type : PieceType.values())
			{
				for (PieceModelSet set : PieceModelSet.values())
				{
					BUFFERS.put(Pair.of(type, set), generate(chessBuilder, level, chessVertexFormat, type, set));
				}
			}
		}
	}
	
	private static VertexBuffer generate(BufferBuilder builder, LODLevel level, VertexFormat format, PieceType type, PieceModelSet set)
	{
		builder.begin(VertexFormat.Mode.TRIANGLES, format);
		CHESS_PIECE_MODEL.render(new PoseStack(), builder, level, type, set, false);
		VertexBuffer buffer = BUFFERS.getOrDefault(level, new HashMap<>()).getOrDefault(Pair.of(type, set), null);
		if (buffer == null) buffer = new VertexBuffer();
		upload(buffer, builder);
		return buffer;
	}
	
	private static void upload(VertexBuffer buffer,BufferBuilder builder)
	{
		buffer.bind();
		buffer.upload(builder.end());
		VertexBuffer.unbind();
		builder.clear(); // frees up unneeded memory
	}
	
	private static HashMap<Pair<PieceType, PieceModelSet>, VertexBuffer> CURRENT_BUFFERS;
	
	public static void level(LODLevel level)
	{
		CURRENT_BUFFERS = BUFFERS.get(level);
	}
	
	public static VertexBuffer getBuffer(PieceModelSet set, PieceType piece)
	{
		return CURRENT_BUFFERS.get(Pair.of(piece, set));
	}
}