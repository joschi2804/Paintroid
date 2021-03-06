/**
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catrobat.paintroid.tools.implementation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import org.catrobat.paintroid.PaintroidApplication;
import org.catrobat.paintroid.command.Command;
import org.catrobat.paintroid.command.LayerBitmapCommand;
import org.catrobat.paintroid.command.UndoRedoManager;
import org.catrobat.paintroid.command.implementation.LayerCommand;
import org.catrobat.paintroid.command.implementation.ResizeCommand;
import org.catrobat.paintroid.dialog.IndeterminateProgressDialog;
import org.catrobat.paintroid.eventlistener.OnUpdateTopBarListener;
import org.catrobat.paintroid.listener.LayerListener;
import org.catrobat.paintroid.tools.Layer;
import org.catrobat.paintroid.tools.Tool;
import org.catrobat.paintroid.tools.ToolType;
import org.catrobat.paintroid.ui.Perspective;

import java.util.LinkedList;


public class UndoTool extends BaseTool {

	private Tool mPreviousTool;
	private Layer mLayer;
	private LayerBitmapCommand mLayerBitmapCommand;
	private LinkedList<Command> mCommandList;
	private boolean mReadyForUndo = false;

	public UndoTool(Context context, ToolType toolType) {
		super(context, toolType);
		mPreviousTool = PaintroidApplication.currentTool;
		mLayer = LayerListener.getInstance().getCurrentLayer();
		LayerCommand layerCommand = new LayerCommand(mLayer);
		mLayerBitmapCommand = PaintroidApplication.commandManager
				.getLayerBitmapCommand(layerCommand);
		showProgressDialog();
		mReadyForUndo = true;
	}



	@Override
	public boolean handleDown(PointF coordinate) {
		return false;
	}

	@Override
	public boolean handleMove(PointF coordinate) {
		return false;
	}

	@Override
	public boolean handleUp(PointF coordinate) {
		return  true;
	}

	@Override
	public void resetInternalState() {
	}

	@Override
	public void draw(Canvas canvas) {
		if(mReadyForUndo){
			PaintroidApplication.currentTool = mPreviousTool;
			mReadyForUndo = false;

			float scale = PaintroidApplication.perspective.getScale();
			float surfaceTranslationX = PaintroidApplication.perspective.getSurfaceTranslationX();
			float surfaceTranslationY = PaintroidApplication.perspective.getSurfaceTranslationY();

			mLayerBitmapCommand.clearLayerBitmap();
			mLayerBitmapCommand.addCommandToUndoList();
			UndoRedoManager.getInstance().update();

			for (Command command : mLayerBitmapCommand.getLayerCommands()) {
				if(command.getClass().equals(ResizeCommand.class)) // doesnt work correct -> remove for release
					continue;
				command.run(PaintroidApplication.drawingSurface.getCanvas(), mLayer.getImage());
			}
			IndeterminateProgressDialog.getInstance().dismiss();
			setPerspective(scale, surfaceTranslationX, surfaceTranslationY);
		}

	}

	@Override
	public void setupToolOptions() {
		mPreviousTool.setupToolOptions();
		ToolType toolType = mPreviousTool.getToolType();
		ToolType.UNDO.setNameResource(toolType.getNameResource());
	}


	private void showProgressDialog() {
		if(mLayerBitmapCommand.getLayerCommands().size() != 0)
			IndeterminateProgressDialog.getInstance().show();
	}

	private void setPerspective(float scale, float translationX, float translationY) {
		PaintroidApplication.perspective.setScale(scale);
		PaintroidApplication.perspective.setSurfaceTranslationX(translationX);
		PaintroidApplication.perspective.setSurfaceTranslationY(translationY);
	}

}
