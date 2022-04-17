package andrews.table_top_craft.screens.chess.menus.color_selection;

import andrews.table_top_craft.screens.piece_figure.util.*;
import andrews.table_top_craft.util.Color;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;

import andrews.table_top_craft.screens.chess.buttons.ChessCancelButton;
import andrews.table_top_craft.screens.chess.buttons.ChessCancelButton.ChessCancelButtonText;
import andrews.table_top_craft.screens.chess.buttons.ChessCancelButton.ChessCancelMenuTarget;
import andrews.table_top_craft.screens.chess.buttons.colors.ChessConfirmColorButton;
import andrews.table_top_craft.screens.chess.buttons.colors.ChessConfirmColorButton.ColorMenuType;
import andrews.table_top_craft.screens.chess.buttons.colors.ChessRandomColorButton;
import andrews.table_top_craft.screens.chess.buttons.colors.ChessResetColorButton;
import andrews.table_top_craft.screens.chess.buttons.colors.ChessResetColorButton.DefaultColorType;
import andrews.table_top_craft.screens.chess.sliders.ChessBlueColorSlider;
import andrews.table_top_craft.screens.chess.sliders.ChessGreenColorSlider;
import andrews.table_top_craft.screens.chess.sliders.ChessRedColorSlider;
import andrews.table_top_craft.tile_entities.ChessTileEntity;
import andrews.table_top_craft.util.NBTColorSaving;
import andrews.table_top_craft.util.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ForgeSlider;

public class ChessBoardTilesColorScreen extends Screen implements IColorPicker, IColorPickerExtended
{
	private static final ResourceLocation MENU_TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/menus/chess_menu.png");
	private static final ResourceLocation PREVIEW_TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/menus/preview_color.png");
	private static final ResourceLocation PREVIEW_FRAME_TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/menus/medium_chess_menu.png");
	private static final ResourceLocation COLOR_PICKER_FRAME_TEXTURE = new ResourceLocation(Reference.MODID, "textures/gui/color_picker/color_picker_frame.png");
	private final String colorSelectionText = new TranslatableComponent("gui.table_top_craft.chess.color.tiles").getString();
	private final String previewColorText = new TranslatableComponent("gui.table_top_craft.chess.color.colors_preview").getString();
	private final String whiteTileSettingsText = new TranslatableComponent("gui.table_top_craft.chess.color.white_tiles_settings").getString();
	private final String blackTileSettingsText = new TranslatableComponent("gui.table_top_craft.chess.color.black_tiles_settings").getString();
	private final ChessTileEntity chessTileEntity;
	private final int xSize = 177;
	private final int ySize = 198;
	private ChessRedColorSlider whiteRedColorSlider;
	private ChessGreenColorSlider whiteGreenColorSlider;
	private ChessBlueColorSlider whiteBlueColorSlider;
	private ChessRedColorSlider blackRedColorSlider;
	private ChessGreenColorSlider blackGreenColorSlider;
	private ChessBlueColorSlider blackBlueColorSlider;
	private TTCColorPicker colorPicker;
	private SaturationSlider saturationSlider;
	private final boolean isColorPickerActive;
	private final boolean isOptionalColorPickerActive;
	
	public ChessBoardTilesColorScreen(ChessTileEntity chessTileEntity, boolean isColorPickerActive, boolean isOptionalColorPickerActive)
	{
		super(new TextComponent(""));
		this.chessTileEntity = chessTileEntity;
		this.isColorPickerActive = isColorPickerActive;
		this.isOptionalColorPickerActive = isOptionalColorPickerActive;
	}
	
	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
	
	@Override
	protected void init()
	{
		super.init();
		// Values to calculate the relative position
		int x = (this.width - (this.xSize - (this.isColorPickerActive || this.isOptionalColorPickerActive ? 136 : 0))) / 2;
		int y = (this.height - this.ySize) / 2;

		this.addRenderableWidget(new ColorPickerToggleButton(this.chessTileEntity, this, false, x + 68, y + 54));
		this.addRenderableWidget(new ColorPickerToggleButton(this.chessTileEntity, this, true, x + 153, y + 54));

		// RGB Sliders
	    this.addRenderableWidget(this.whiteRedColorSlider = new ChessRedColorSlider(x + 5, y + 92, 167, 12, NBTColorSaving.getRed(this.chessTileEntity.getWhiteTilesColor()), this));
	    this.addRenderableWidget(this.whiteGreenColorSlider = new ChessGreenColorSlider(x + 5, y + 105, 167, 12, NBTColorSaving.getGreen(this.chessTileEntity.getWhiteTilesColor()), this));
	    this.addRenderableWidget(this.whiteBlueColorSlider = new ChessBlueColorSlider(x + 5, y + 118, 167, 12, NBTColorSaving.getBlue(this.chessTileEntity.getWhiteTilesColor()), this));
	    this.addRenderableWidget(this.blackRedColorSlider = new ChessRedColorSlider(x + 5, y + 141, 167, 12, NBTColorSaving.getRed(this.chessTileEntity.getBlackTilesColor()), this));
	    this.addRenderableWidget(this.blackGreenColorSlider = new ChessGreenColorSlider(x + 5, y + 154, 167, 12, NBTColorSaving.getGreen(this.chessTileEntity.getBlackTilesColor()), this));
	    this.addRenderableWidget(this.blackBlueColorSlider = new ChessBlueColorSlider(x + 5, y + 167, 167, 12, NBTColorSaving.getBlue(this.chessTileEntity.getBlackTilesColor()), this));
		
		// The Buttons in the Gui Menu
	    this.addRenderableWidget(new ChessRandomColorButton(this, x + 5, y + 68));
	    this.addRenderableWidget(new ChessResetColorButton(DefaultColorType.BOARD_TILES, this, x + 90, y + 68));
	    this.addRenderableWidget(new ChessCancelButton(this.chessTileEntity, ChessCancelMenuTarget.CHESS_BOARD_COLORS, ChessCancelButtonText.CANCEL, x + 5, y + 180));
	    this.addRenderableWidget(new ChessConfirmColorButton(ColorMenuType.BOARD_PLATE, this.chessTileEntity, this.whiteRedColorSlider, this.blackRedColorSlider, this.whiteGreenColorSlider, this.blackGreenColorSlider, this.whiteBlueColorSlider, this.blackBlueColorSlider, x + 90, y + 180));

		if(isColorPickerActive)
		{
			// The Color Picker
			Color color = new Color(whiteRedColorSlider.getValueInt(), whiteGreenColorSlider.getValueInt(), whiteBlueColorSlider.getValueInt());
			this.addRenderableWidget(colorPicker = new TTCColorPicker(x - 131, y + 28, this, color.getHue() / 360F, 1.0F - color.getValue()));
			// The saturation Slider
			float saturation = color.getSaturation() * 100;
			this.addRenderableWidget(saturationSlider = new SaturationSlider(x - 132, y + 158, 130, 12, Math.round(saturation), this));
		}
		else if(isOptionalColorPickerActive)
		{
			// The Color Picker
			Color color = new Color(blackRedColorSlider.getValueInt(), blackGreenColorSlider.getValueInt(), blackBlueColorSlider.getValueInt());
			this.addRenderableWidget(colorPicker = new TTCColorPicker(x - 131, y + 28, this, color.getHue() / 360F, 1.0F - color.getValue()));
			// The saturation Slider
			float saturation = color.getSaturation() * 100;
			this.addRenderableWidget(saturationSlider = new SaturationSlider(x - 132, y + 158, 130, 12, Math.round(saturation), this));
		}
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		int x = (this.width - (this.xSize - (this.isColorPickerActive || this.isOptionalColorPickerActive ? 136 : 0))) / 2;
		int y = (this.height - this.ySize) / 2;

		if(isColorPickerActive || isOptionalColorPickerActive)
		{
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
			RenderSystem.setShaderTexture(0, COLOR_PICKER_FRAME_TEXTURE);
			this.blit(poseStack, x - 136, y + 23, 0, 0, 136, 151);
		}

		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, MENU_TEXTURE);
		this.blit(poseStack, x, y, 0, 0, this.xSize, this.ySize);
		
		// The Preview of the Color
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, PREVIEW_FRAME_TEXTURE);
		this.blit(poseStack, x + 25, y + 25, 0, 131, 42, 42);
		this.blit(poseStack, x + 110, y + 25, 0, 131, 42, 42);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		RenderSystem.setShaderTexture(0, PREVIEW_TEXTURE);
		// White Color Preview
		poseStack.pushPose();
		RenderSystem.setShaderColor((1F / 255F) * this.whiteRedColorSlider.getValueInt(), (1F / 255F) * this.whiteGreenColorSlider.getValueInt(), (1F / 255F) * this.whiteBlueColorSlider.getValueInt(), 1.0f);
		poseStack.translate(x + 26, y + 26, 0);
		poseStack.scale(2.5F, 2.5F, 2.5F);
		this.blit(poseStack, 0, 0, 0, 0, 16, 16);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		poseStack.popPose();
		// Black Color Preview
		poseStack.pushPose();
		RenderSystem.setShaderColor((1F / 255F) * this.blackRedColorSlider.getValueInt(), (1F / 255F) * this.blackGreenColorSlider.getValueInt(), (1F / 255F) * this.blackBlueColorSlider.getValueInt(), 1.0f);
		poseStack.translate(x + 111, y + 26, 0);
		poseStack.scale(2.5F, 2.5F, 2.5F);
		this.blit(poseStack, 0, 0, 0, 0, 16, 16);
		RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
		poseStack.popPose();
		
		// The Menu Title
		this.font.draw(poseStack, this.colorSelectionText, ((this.width / 2) - (this.font.width(this.colorSelectionText) / 2)) + (this.isColorPickerActive || this.isOptionalColorPickerActive ? 68 : 0), y + 6, 4210752);
		// The preview String
		this.font.draw(poseStack, this.previewColorText, x + 88 - (this.font.width(this.previewColorText) / 2), y + 16, 0x000000);
		// The color descriptions above the sliders
		this.font.draw(poseStack, this.whiteTileSettingsText, x + 5, y + 83, 0x000000);
		this.font.draw(poseStack, this.blackTileSettingsText, x + 5, y + 132, 0x000000);
		// Renders the Buttons we added in init
		super.render(poseStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		super.keyPressed(keyCode, scanCode, modifiers);
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if(this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey))
			this.onClose();//TODO make sure this works
		return true;
	}

	@Override
	public TTCColorPicker getColorPicker()
	{
		return this.colorPicker;
	}

	@Override
	public ForgeSlider getRedSlider()
	{
		return this.whiteRedColorSlider;
	}

	@Override
	public ForgeSlider getGreenSlider()
	{
		return this.whiteGreenColorSlider;
	}

	@Override
	public ForgeSlider getBlueSlider()
	{
		return this.whiteBlueColorSlider;
	}

	@Override
	public ForgeSlider getSaturationSlider()
	{
		return this.saturationSlider;
	}

	@Override
	public boolean isColorPickerActive()
	{
		return this.isColorPickerActive;
	}

	@Override
	public ForgeSlider getOptionalRedSlider()
	{
		return this.blackRedColorSlider;
	}

	@Override
	public ForgeSlider getOptionalGreenSlider()
	{
		return this.blackGreenColorSlider;
	}

	@Override
	public ForgeSlider getOptionalBlueSlider()
	{
		return this.blackBlueColorSlider;
	}

	@Override
	public boolean isOptionalColorPickerActive()
	{
		return this.isOptionalColorPickerActive;
	}
}