package com.simibubi.create.compat.rei;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.base.Predicates;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.Create;
import com.simibubi.create.compat.rei.category.BlockCuttingCategory;
import com.simibubi.create.compat.rei.category.BlockCuttingCategory.CondensedBlockCuttingRecipe;
import com.simibubi.create.compat.rei.category.CreateRecipeCategory;
import com.simibubi.create.compat.rei.category.CrushingCategory;
import com.simibubi.create.compat.rei.category.DeployingCategory;
import com.simibubi.create.compat.rei.category.FanBlastingCategory;
import com.simibubi.create.compat.rei.category.FanSmokingCategory;
import com.simibubi.create.compat.rei.category.FanWashingCategory;
import com.simibubi.create.compat.rei.category.ItemDrainCategory;
import com.simibubi.create.compat.rei.category.MechanicalCraftingCategory;
import com.simibubi.create.compat.rei.category.MillingCategory;
import com.simibubi.create.compat.rei.category.MixingCategory;
import com.simibubi.create.compat.rei.category.MysteriousItemConversionCategory;
import com.simibubi.create.compat.rei.category.PackingCategory;
import com.simibubi.create.compat.rei.category.PolishingCategory;
import com.simibubi.create.compat.rei.category.PressingCategory;
import com.simibubi.create.compat.rei.category.ProcessingViaFanCategory;
import com.simibubi.create.compat.rei.category.SawingCategory;
import com.simibubi.create.compat.rei.category.SequencedAssemblyCategory;
import com.simibubi.create.compat.rei.category.SpoutCategory;
import com.simibubi.create.compat.rei.display.AbstractCreateDisplay;
import com.simibubi.create.compat.rei.display.AutomaticPackingDisplay;
import com.simibubi.create.compat.rei.display.BlockCuttingDisplay;
import com.simibubi.create.compat.rei.display.CrushingDisplay;
import com.simibubi.create.compat.rei.display.DeployingDisplay;
import com.simibubi.create.compat.rei.display.FanBlastingDisplay;
import com.simibubi.create.compat.rei.display.FanSmokingDisplay;
import com.simibubi.create.compat.rei.display.FanWashingDisplay;
import com.simibubi.create.compat.rei.display.ItemDrainDisplay;
import com.simibubi.create.compat.rei.display.MechanicalCraftingDisplay;
import com.simibubi.create.compat.rei.display.MillingDisplay;
import com.simibubi.create.compat.rei.display.MixingDisplay;
import com.simibubi.create.compat.rei.display.MysteriousItemConversionDisplay;
import com.simibubi.create.compat.rei.display.PackingDisplay;
import com.simibubi.create.compat.rei.display.PolishingDisplay;
import com.simibubi.create.compat.rei.display.PressingDisplay;
import com.simibubi.create.compat.rei.display.SawingDisplay;
import com.simibubi.create.compat.rei.display.SequencedAssemblyDisplay;
import com.simibubi.create.compat.rei.display.SpoutDisplay;
import com.simibubi.create.content.contraptions.components.crusher.AbstractCrushingRecipe;
import com.simibubi.create.content.contraptions.components.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.contraptions.components.fan.SplashingRecipe;
import com.simibubi.create.content.contraptions.components.millstone.MillingRecipe;
import com.simibubi.create.content.contraptions.components.press.MechanicalPressTileEntity;
import com.simibubi.create.content.contraptions.components.press.PressingRecipe;
import com.simibubi.create.content.contraptions.components.saw.CuttingRecipe;
import com.simibubi.create.content.contraptions.components.saw.SawTileEntity;
import com.simibubi.create.content.contraptions.fluids.actors.FillingRecipe;
import com.simibubi.create.content.contraptions.fluids.recipe.PotionMixingRecipeManager;
import com.simibubi.create.content.contraptions.itemAssembly.SequencedAssemblyRecipe;
import com.simibubi.create.content.contraptions.processing.BasinRecipe;
import com.simibubi.create.content.contraptions.processing.EmptyingRecipe;
import com.simibubi.create.content.curiosities.tools.SandPaperPolishingRecipe;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.config.ConfigBase.ConfigBool;
import com.simibubi.create.foundation.gui.container.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.utility.recipe.IRecipeTypeInfo;

import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ExclusionZones;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.level.ItemLike;

@SuppressWarnings("unused")
public class CreateREI implements REIClientPlugin {

	private static final ResourceLocation ID = Create.asResource("rei_plugin");

//	public IIngredientManager ingredientManager;
	private final List<CreateRecipeCategory> allCategories = new ArrayList<>();
//	private final CreateRecipeCategory<?, ?>

	private final CreateRecipeCategory milling = register("milling", MillingCategory::new).recipes(AllRecipeTypes.MILLING)
		.catalyst(AllBlocks.MILLSTONE::get)
		.build();

	private final CreateRecipeCategory crushing = register("crushing", CrushingCategory::new).recipes(AllRecipeTypes.CRUSHING)
			.recipesExcluding(AllRecipeTypes.MILLING::getType, AllRecipeTypes.CRUSHING::getType)
			.catalyst(AllBlocks.CRUSHING_WHEEL::get)
			.build();

	private final CreateRecipeCategory pressing = register("pressing", PressingCategory::new).recipes(AllRecipeTypes.PRESSING)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.build();

	private final CreateRecipeCategory washing = register("fan_washing", FanWashingCategory::new).recipes(AllRecipeTypes.SPLASHING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_washing"))
			.build();

	private final CreateRecipeCategory smoking = register("fan_smoking", FanSmokingCategory::new).recipes(() -> RecipeType.SMOKING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_smoking"))
			.build();

	private final CreateRecipeCategory blasting = register("fan_blasting", FanBlastingCategory::new)
			.recipesExcluding(() -> RecipeType.SMELTING, () -> RecipeType.BLASTING)
			.recipes(() -> RecipeType.BLASTING)
			.removeRecipes(() -> RecipeType.SMOKING)
			.catalystStack(ProcessingViaFanCategory.getFan("fan_blasting"))
			.build();

	private final CreateRecipeCategory mixing = register("mixing", MixingCategory::standard).recipes(AllRecipeTypes.MIXING::getType)
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.build();

	private final CreateRecipeCategory seqAssembly = register("sequenced_assembly", SequencedAssemblyCategory::new)
			.recipes(AllRecipeTypes.SEQUENCED_ASSEMBLY::getType)
			.build();

	private final CreateRecipeCategory autoShapeless = register("automatic_shapeless", MixingCategory::autoShapeless)
			.recipes(r -> ((BasinRecipe)r).getSerializer() == RecipeSerializer.SHAPELESS_RECIPE && ((BasinRecipe)r).getIngredients()
				.size() > 1 && !MechanicalPressTileEntity.canCompress((BasinRecipe)r),
				r -> BasinRecipe.convertShapeless((BasinRecipe)r))
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.enableWhen(c -> ((CRecipes)c).allowShapelessInMixer)
			.build();

	private final CreateRecipeCategory brewing = register("automatic_brewing", MixingCategory::autoBrewing)
			.recipeList(PotionMixingRecipeManager::getAllBrewingRecipes)
			.catalyst(AllBlocks.MECHANICAL_MIXER::get)
			.catalyst(AllBlocks.BASIN::get)
			.build();

	private final CreateRecipeCategory sawing = register("sawing", SawingCategory::new).recipes(AllRecipeTypes.CUTTING)
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.build();

	private final CreateRecipeCategory blockCutting = register("block_cutting", () -> new BlockCuttingCategory(Items.STONE_BRICK_STAIRS))
			.recipeList(() -> CondensedBlockCuttingRecipe.condenseRecipes(findRecipesByType(RecipeType.STONECUTTING)))
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.enableWhen(c -> ((CRecipes)c).allowStonecuttingOnSaw)
			.build();

	private final CreateRecipeCategory woodCutting = register("wood_cutting", () -> new BlockCuttingCategory(Items.OAK_STAIRS))
			.recipeList(() -> CondensedBlockCuttingRecipe
				.condenseRecipes(findRecipesByType(SawTileEntity.woodcuttingRecipeType.get())))
			.catalyst(AllBlocks.MECHANICAL_SAW::get)
			.enableWhenBool(c -> ((CRecipes)c).allowWoodcuttingOnSaw.get() && FabricLoader.getInstance()
				.isModLoaded("druidcraft"))
			.build();

	private final CreateRecipeCategory packing = register("packing", PackingCategory::standard).recipes(AllRecipeTypes.COMPACTING)
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.catalyst(AllBlocks.BASIN::get)
			.build();

	private final CreateRecipeCategory autoSquare = register("automatic_packing", PackingCategory::autoSquare)
			.recipes(re -> (re instanceof CraftingRecipe r) && MechanicalPressTileEntity.canCompress(r),
					(r) -> BasinRecipe.convertShapeless((Recipe<?>) r))
			.catalyst(AllBlocks.MECHANICAL_PRESS::get)
			.catalyst(AllBlocks.BASIN::get)
			.enableWhen(c -> ((CRecipes)c).allowShapedSquareInPress)
			.build();

	private final CreateRecipeCategory polishing = register("sandpaper_polishing", PolishingCategory::new).recipes(AllRecipeTypes.SANDPAPER_POLISHING)
			.catalyst(AllItems.SAND_PAPER::get)
			.catalyst(AllItems.RED_SAND_PAPER::get)
			.build();

	private final CreateRecipeCategory deploying = register("deploying", DeployingCategory::new)
			.recipeList(
				() -> DeployerApplicationRecipe.convert(findRecipesByType(AllRecipeTypes.SANDPAPER_POLISHING.getType())))
			.recipes(AllRecipeTypes.DEPLOYING)
			.catalyst(AllBlocks.DEPLOYER::get)
			.catalyst(AllBlocks.DEPOT::get)
			.catalyst(AllItems.BELT_CONNECTOR::get)
			.build();

	private final CreateRecipeCategory mysteryConversion = register("mystery_conversion", MysteriousItemConversionCategory::new)
			.recipeList(MysteriousItemConversionCategory::getRecipes)
			.build();

	private final CreateRecipeCategory spoutFilling = register("spout_filling", SpoutCategory::new).recipes(AllRecipeTypes.FILLING)
			/*.recipeList(() -> SpoutCategory.getRecipes(ingredientManager))*/
			.catalyst(AllBlocks.SPOUT::get)
			.build();

	private final CreateRecipeCategory draining = register("draining", ItemDrainCategory::new)
			/*.recipeList(() -> ItemDrainCategory.getRecipes(ingredientManager))*/
			.recipes(AllRecipeTypes.EMPTYING)
			.catalyst(AllBlocks.ITEM_DRAIN::get)
			.build();

//	private final CreateRecipeCategory autoShaped = register("automatic_shaped", MechanicalCraftingCategory::new)
//			.recipes(r -> r.getSerializer() == RecipeSerializer.SHAPELESS_RECIPE && r.getIngredients()
//				.size() == 1)
//			.recipes(
//				r -> (r.getType() == RecipeType.CRAFTING && r.getType() != AllRecipeTypes.MECHANICAL_CRAFTING.getType())
//					&& (r instanceof ShapedRecipe))
//			.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
//			.enableWhen(c -> c.allowRegularCraftingInCrafter)
//			.build();

	private final CreateRecipeCategory mechanicalCrafting =
			register("mechanical_crafting", MechanicalCraftingCategory::new).recipes(AllRecipeTypes.MECHANICAL_CRAFTING)
				.catalyst(AllBlocks.MECHANICAL_CRAFTER::get)
				.build();

	private <T extends Recipe<?>, D extends AbstractCreateDisplay<T>> CategoryBuilder register(String name,
		Supplier<CreateRecipeCategory<T, D>> supplier) {
		return new CategoryBuilder<>(name, supplier);
	}

	@Override
	public String getPluginProviderName() {
		return ID.toString();
	}

//	@Override
//	public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
//		registration.addRecipeTransferHandler(new BlueprintTransferHandler(), VanillaRecipeCategoryUid.CRAFTING);
//	}

	@Override
	public void registerCategories(CategoryRegistry registry) {
		allCategories.forEach(registry::add);
		allCategories.forEach(createRecipeCategory -> registry.removePlusButton(createRecipeCategory.getCategoryIdentifier()));
		allCategories.forEach(c -> c.recipeCatalysts.forEach(s -> registry.addWorkstations(c.getCategoryIdentifier(), EntryStack.of(VanillaEntryTypes.ITEM, ((Supplier<ItemStack> )s).get()))));
	}

	@Override
	public void registerDisplays(DisplayRegistry registry) {
		registry.registerFiller(AbstractCrushingRecipe.class, CrushingDisplay::new);
		registry.registerFiller(MillingRecipe.class, MillingDisplay::new);
		registry.registerFiller(PressingRecipe.class, PressingDisplay::new);
		registry.registerFiller(CuttingRecipe.class, SawingDisplay::new);
		registry.registerFiller(FillingRecipe.class, SpoutDisplay::new);
		registry.registerFiller(CraftingRecipe.class, MechanicalCraftingDisplay::new);
		registry.registerFiller(SmokingRecipe.class, FanSmokingDisplay::new);
		registry.registerFiller(AbstractCookingRecipe.class, FanBlastingDisplay::new);
		registry.registerFiller(SplashingRecipe.class, FanWashingDisplay::new);
		registry.registerFiller(DeployerApplicationRecipe.class, DeployingDisplay::new);
		registry.registerFiller(SequencedAssemblyRecipe.class, SequencedAssemblyDisplay::new);
		registry.registerFiller(ConversionRecipe.class, MysteriousItemConversionDisplay::new);
		registry.registerFiller(EmptyingRecipe.class, ItemDrainDisplay::new);
		registry.registerFiller(BasinRecipe.class, MixingDisplay::new);
		registry.registerFiller(BasinRecipe.class, MixingDisplay::shapeless);
		registry.registerFiller(BasinRecipe.class, MixingDisplay::autoBrewing);
		registry.registerFiller(BasinRecipe.class, PackingDisplay::new);
		registry.registerFiller(BasinRecipe.class, AutomaticPackingDisplay::new);
		registry.registerFiller(SandPaperPolishingRecipe.class, PolishingDisplay::new);
		registry.registerFiller(CondensedBlockCuttingRecipe.class, BlockCuttingDisplay::new);
		registry.registerFiller(CondensedBlockCuttingRecipe.class, BlockCuttingDisplay::woodCutting);
	}

	//	@Override
//	public void registerRecipes(IRecipeRegistration registration) {
//		ingredientManager = registration.getIngredientManager();
//		allCategories.forEach(c -> c.recipes.forEach(s -> registration.addRecipes(s.get(), c.getUid())));
//	}

//	@SuppressWarnings({ "unchecked", "rawtypes" })
//	@Override
//	public void registerGuiHandlers(IGuiHandlerRegistration registration) {
//		registration.addGenericGuiContainerHandler(AbstractSimiContainerScreen.class, new SlotMover());
//
//		registration.addGhostIngredientHandler(AbstractFilterScreen.class, new GhostIngredientHandler());
//		registration.addGhostIngredientHandler(BlueprintScreen.class, new GhostIngredientHandler());
//		registration.addGhostIngredientHandler(LinkedControllerScreen.class, new GhostIngredientHandler());
//	}


	@Override
	public void registerExclusionZones(ExclusionZones zones) {
		zones.register(AbstractSimiContainerScreen.class, new SlotMover());
	}

	private class CategoryBuilder<T extends Recipe<?>, D extends AbstractCreateDisplay<T>> {
		private CreateRecipeCategory<T, D> category;
		private AbstractCreateDisplay<T> display;
		private List<Consumer<List<Recipe<?>>>> recipeListConsumers = new ArrayList<>();
		private Predicate<CRecipes> pred;

		public CategoryBuilder(String name, Supplier<CreateRecipeCategory<T, D>> category) {
			this.category = category.get();
			this.category.setCategoryId(name);
			pred = Predicates.alwaysTrue();
		}

		public CategoryBuilder<T, D> recipes(IRecipeTypeInfo recipeTypeEntry) {
			return recipes(recipeTypeEntry::getType);
		}

		public CategoryBuilder<T, D> recipes(Supplier<RecipeType<? extends T>> recipeType) {
			return recipes(r -> r.getType() == recipeType.get());
		}

		public CategoryBuilder<T, D> recipes(ResourceLocation serializer) {
			return recipes(r -> Registry.RECIPE_SERIALIZER.getKey(r.getSerializer())
				.equals(serializer));
		}

		public CategoryBuilder<T, D> recipes(Predicate<Recipe<?>> pred) {
			return recipeList(() -> findRecipes(pred));
		}

		public CategoryBuilder<T, D> recipes(Predicate<Recipe<?>> pred, Function<Recipe<?>, T> converter) {
			return recipeList(() -> findRecipes(pred), converter);
		}

		public CategoryBuilder<T, D> recipeList(Supplier<List<? extends Recipe<?>>> list) {
			return recipeList(list, null);
		}

		public CategoryBuilder<T, D> recipeList(Supplier<List<? extends Recipe<?>>> list,
			Function<Recipe<?>, T> converter) {
			recipeListConsumers.add(recipes -> {
				List<? extends Recipe<?>> toAdd = list.get();
				if (converter != null)
					toAdd = toAdd.stream()
						.map(converter)
						.collect(Collectors.toList());
				recipes.addAll(toAdd);
			});
			return this;
		}

		public CategoryBuilder<T, D> recipesExcluding(Supplier<RecipeType<? extends T>> recipeType,
			Supplier<RecipeType<? extends T>> excluded) {
			recipeListConsumers.add(recipes -> {
				recipes.addAll(findRecipesByTypeExcluding(recipeType.get(), excluded.get()));
			});
			return this;
		}

		public CategoryBuilder<T, D> removeRecipes(Supplier<RecipeType<? extends T>> recipeType) {
			recipeListConsumers.add(recipes -> {
				removeRecipesByType(recipes, recipeType.get());
			});
			return this;
		}

		public CategoryBuilder<T, D> catalyst(Supplier<ItemLike> supplier) {
			return catalystStack(() -> new ItemStack(supplier.get()
				.asItem()));
		}

		public CategoryBuilder<T, D> catalystStack(Supplier<ItemStack> supplier) {
			category.recipeCatalysts.add(supplier);
			return this;
		}

		public CategoryBuilder<T, D> enableWhen(Function<CRecipes, ConfigBool> configValue) {
			pred = c -> configValue.apply(c)
				.get();
			return this;
		}

		public CategoryBuilder<T, D> enableWhenBool(Function<CRecipes, Boolean> configValue) {
			pred = configValue::apply;
			return this;
		}

		public CreateRecipeCategory<T, D> build() {
//			if (pred.test(AllConfigs.SERVER.recipes))
//				category.recipes.add(() -> {
//					List<Recipe<?>> recipes = new ArrayList<>();
//					for (Consumer<List<Recipe<?>>> consumer : recipeListConsumers)
//						consumer.accept(recipes);
//					return recipes;
//				});
			allCategories.add(category);
			return category;
		}

	}

	public static List<Recipe<?>> findRecipes(Predicate<Recipe<?>> predicate) {
		return Minecraft.getInstance().level.getRecipeManager()
			.getRecipes()
			.stream()
			.filter(predicate)
			.collect(Collectors.toList());
	}

	public static List<Recipe<?>> findRecipesByType(RecipeType<?> type) {
		return findRecipes(recipe -> recipe.getType() == type);
	}

	public static List<Recipe<?>> findRecipesByTypeExcluding(RecipeType<?> type, RecipeType<?> excludingType) {
		List<Recipe<?>> byType = findRecipesByType(type);
		removeRecipesByType(byType, excludingType);
		return byType;
	}

	public static List<Recipe<?>> findRecipesByTypeExcluding(RecipeType<?> type, RecipeType<?>... excludingTypes) {
		List<Recipe<?>> byType = findRecipesByType(type);
		for (RecipeType<?> excludingType : excludingTypes)
			removeRecipesByType(byType, excludingType);
		return byType;
	}

	public static void removeRecipesByType(List<Recipe<?>> recipes, RecipeType<?> type) {
		List<Recipe<?>> byType = findRecipesByType(type);
		recipes.removeIf(recipe -> {
			for (Recipe<?> r : byType)
				if (doInputsMatch(recipe, r))
					return true;
			return false;
		});
	}

	public static boolean doInputsMatch(Recipe<?> recipe1, Recipe<?> recipe2) {
		ItemStack[] matchingStacks = recipe1.getIngredients()
			.get(0)
			.getItems();
		if (matchingStacks.length == 0)
			return true;
		if (recipe2.getIngredients()
			.get(0)
			.test(matchingStacks[0]))
			return true;
		return false;
	}

}
