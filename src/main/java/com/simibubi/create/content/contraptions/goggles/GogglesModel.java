package com.simibubi.create.content.contraptions.goggles;

import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.lib.render.TransformTypeDependentItemBakedModel;

import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.BakedModel;

public class GogglesModel extends ForwardingBakedModel implements TransformTypeDependentItemBakedModel {

	public GogglesModel(BakedModel template) {
		wrapped = template;
	}

	@Override
	public BakedModel create$handlePerspective(TransformType cameraTransformType) {
		if (cameraTransformType == TransformType.HEAD)
			return AllBlockPartials.GOGGLES.get();
		return this;
	}

}
