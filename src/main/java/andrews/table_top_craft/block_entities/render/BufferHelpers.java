package andrews.table_top_craft.block_entities.render;

import andrews.table_top_craft.util.Color;
import andrews.table_top_craft.util.TTCRenderTypes;
import com.mojang.blaze3d.platform.GlDebug;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public class BufferHelpers {
	public static boolean useFallbackSystem = true;
	public static boolean shouldRefresh = true;
	
	public static boolean useVanillaShader = false;
	
	public static int currentU;
	public static int currentV;
	
	public static void setupRender(ShaderInstance pShaderInstance, int lightU, int ilghtV /* GiantLuigi4 (Jason): no I will not correct this typo */) {
		useVanillaShader = true;
		
		pShaderInstance.apply();
		
		currentU = lightU;
		currentV = ilghtV;
		
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
		
		if (shouldRefresh || useFallbackSystem) {
			RenderSystem.assertOnRenderThread();
			BufferUploader.reset();
			
			RenderSystem.setShaderTexture(0, texture.getId());
			for (int i = 0; i < 12; ++i) {
				int j = RenderSystem.getShaderTexture(i);
				pShaderInstance.setSampler("Sampler" + i, j);

//				if (useVanillaShader) {
				int k = Uniform.glGetUniformLocation(pShaderInstance.getId(), "Sampler" + i);
				Uniform.uploadInteger(k, j);
				RenderSystem.activeTexture('\u84c0' + j);
				RenderSystem.enableTexture();
//					Object object = pShaderInstance.samplerMap.get(s);
//					int l = -1;
//					if (object instanceof RenderTarget) {
//						l = ((RenderTarget)object).getColorTextureId();
//					} else if (object instanceof AbstractTexture) {
//						l = ((AbstractTexture)object).getId();
//					} else if (object instanceof Integer) {
//						l = (Integer)object;
//					}
				int l = j;
				
				if (l != -1) {
					RenderSystem.bindTexture(l);
				}
//				}
			}
			
			if (pShaderInstance.TEXTURE_MATRIX != null)
				pShaderInstance.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
			if (pShaderInstance.GAME_TIME != null) pShaderInstance.GAME_TIME.set(RenderSystem.getShaderGameTime());
			if (pShaderInstance.SCREEN_SIZE != null) {
				Window window = Minecraft.getInstance().getWindow();
				pShaderInstance.SCREEN_SIZE.set((float) window.getWidth(), (float) window.getHeight());
			}
			if (pShaderInstance.LIGHT0_DIRECTION != null)
				pShaderInstance.LIGHT0_DIRECTION.set(0.457495710997814F, 0.7624928516630234F, -0.457495710997814F);
			if (pShaderInstance.LIGHT1_DIRECTION != null)
				pShaderInstance.LIGHT1_DIRECTION.set(-0.27617238536949695F, 0.9205746178983233F, 0.27617238536949695F);
			
			shouldRefresh = false;
		}
		
		if (pShaderInstance.FOG_START != null) {
			pShaderInstance.FOG_START.set(RenderSystem.getShaderFogStart());
			pShaderInstance.FOG_START.upload();
		}
		if (pShaderInstance.FOG_END != null) {
			pShaderInstance.FOG_END.set(RenderSystem.getShaderFogEnd());
			pShaderInstance.FOG_END.upload();
		}
		if (pShaderInstance.FOG_COLOR != null) {
			pShaderInstance.FOG_COLOR.set(RenderSystem.getShaderFogColor());
			pShaderInstance.FOG_COLOR.upload();
		}
		if (pShaderInstance.FOG_SHAPE != null) {
			pShaderInstance.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
			pShaderInstance.FOG_SHAPE.upload();
		}

//		if (!useVanillaShader) {
		Uniform uniform = pShaderInstance.getUniform("LightUV");
		if (uniform != null) {
			uniform.set((float) lightU, ilghtV);
			uniform.upload();
		}
//		} else {
//			if (pShaderInstance.LIGHT0_DIRECTION != null)
//				pShaderInstance.LIGHT0_DIRECTION.set(0.457495710997814F, 0.7624928516630234F, -0.457495710997814F);
//			if (pShaderInstance.LIGHT1_DIRECTION != null)
//				pShaderInstance.LIGHT1_DIRECTION.set(-0.27617238536949695F, 0.9205746178983233F, 0.27617238536949695F);
//		}
		if (pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX != null) {
			pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
			pShaderInstance.INVERSE_VIEW_ROTATION_MATRIX.upload();
		}
		if (pShaderInstance.PROJECTION_MATRIX != null)
			pShaderInstance.PROJECTION_MATRIX.upload();
	}
	
	// Dynamic Texture
	private static final NativeImage image = new NativeImage(NativeImage.Format.RGBA, 1, 1, true);
	private static final DynamicTexture texture = new DynamicTexture(image);
	private static ResourceLocation resourceLocation = null;
	
	static {
		image.setPixelRGBA(0, 0, 16777215);
		texture.upload();
		resourceLocation = Minecraft.getInstance().getTextureManager().register("table_top_craft_dummy", texture);
	}
	
	public static void updateColor(ShaderInstance pShaderInstance, float[] floats) {
		if (useVanillaShader) {
			LightTexture ltexture = Minecraft.getInstance().gameRenderer.lightTexture();
			int RGBA = ltexture.lightPixels.getPixelRGBA(currentU, currentV);

			// TODO: figure out blending here
			if (pShaderInstance.COLOR_MODULATOR != null) {
				Color lColor = new Color(RGBA);
				pShaderInstance.COLOR_MODULATOR.set(new float[]{
						lColor.getRed() / 255f,
						lColor.getGreen() / 255f,
						lColor.getBlue() / 255f,
						lColor.getAlpha() / 255f
				});
				pShaderInstance.COLOR_MODULATOR.upload();
			}

			image.setPixelRGBA(
					0,
					0,
					new Color(
							(int) (floats[0] * 255),
							(int) (floats[1] * 255),
							(int) (floats[2] * 255),
							(int) (floats[3] * 255)
					).getRGB()
			);
			texture.upload();
		} else {
			if (pShaderInstance.COLOR_MODULATOR != null) {
				pShaderInstance.COLOR_MODULATOR.set(floats);
				pShaderInstance.COLOR_MODULATOR.upload();
			}
		}
	}
	
	public static void draw(VertexBuffer buffer) {
		if (buffer != null) {
			buffer.bind();
			buffer.draw();
		}
	}
	
	public static RenderType getRenderType() {
		if (useVanillaShader) {
//			return RenderType.beaconBeam(resourceLocation, false);
			return RenderType.entitySolid(resourceLocation);
		} else {
			return TTCRenderTypes.getChessPieceSolid(resourceLocation);
		}
	}
}
