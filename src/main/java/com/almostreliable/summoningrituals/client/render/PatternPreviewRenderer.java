package com.almostreliable.summoningrituals.client.render;

import com.almostreliable.summoningrituals.altar.AltarBlock;
import com.almostreliable.summoningrituals.client.util.AlphaBufferSource;
import com.almostreliable.summoningrituals.core.Config;
import com.almostreliable.summoningrituals.core.Registration;
import com.almostreliable.summoningrituals.data.SummoningLang;
import com.almostreliable.summoningrituals.recipe.condition.custom.BlockPatternCheck;
import com.almostreliable.summoningrituals.recipe.condition.custom.BlockPatternCheck.ClientPatternEntry;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class PatternPreviewRenderer {

    public static final PatternPreviewRenderer INSTANCE = new PatternPreviewRenderer();
    private static final float PREVIEW_ALPHA = 0.5f;
    private static final float SCALE_FACTOR = 0.75f;
    private static final int TAG_CYCLE_TICKS = 20;

    @Nullable
    private Task task;

    public void render(PoseStack poseStack, Camera camera) {
        if (task == null) return;

        var mc = Minecraft.getInstance();
        var level = mc.level;
        if (level == null) return;

        var age = level.getGameTime() - task.createdAt;
        if (age >= Config.CLIENT.patternPreviewTicks.getAsInt()) {
            task = null;
            return;
        }

        var blockRenderer = mc.getBlockRenderer();
        var cameraPos = camera.getPosition();

        RenderSystem.disableCull();
        ModelBlockRenderer.enableCaching();

        try {
            AlphaBufferSource.INSTANCE.renderWithAlpha(
                PREVIEW_ALPHA, alphaBuffer -> {
                    var cycleStep = age / TAG_CYCLE_TICKS;

                    for (var entry : task.pattern) {
                        var blockStates = entry.blocks();
                        if (blockStates.isEmpty()) continue;

                        var worldPos = task.altarPos.offset(entry.offset());
                        var translation = Vec3.atLowerCornerOf(worldPos).subtract(cameraPos);
                        var blockState = blockStates.get((int) (cycleStep % blockStates.size()));

                        poseStack.pushPose();
                        {
                            poseStack.translate(translation.x, translation.y, translation.z);

                            poseStack.summoning$translate(0.5f);
                            poseStack.summoning$scale(SCALE_FACTOR);
                            poseStack.summoning$translate(-0.5f);

                            //noinspection DataFlowIssue
                            blockRenderer.renderSingleBlock(
                                blockState,
                                poseStack,
                                alphaBuffer,
                                LightTexture.FULL_BRIGHT,
                                OverlayTexture.NO_OVERLAY,
                                ModelData.EMPTY,
                                null
                            );
                        }
                        poseStack.popPose();
                    }
                }
            );
        } finally {
            RenderSystem.enableCull();
            ModelBlockRenderer.clearCache();
        }
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    public static void scheduleTask(BlockPatternCheck blockPatternCheck) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        var level = mc.level;
        if (player == null || level == null) return;

        mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
        player.closeContainer();

        var playerPos = player.position();
        var searchRadius = Config.CLIENT.patternPreviewSearchRadius.getAsInt();
        var searchArea = AABB.ofSize(playerPos, searchRadius, searchRadius, searchRadius);
        var altarSearchEntries = BlockPos.betweenClosedStream(searchArea)
            .map(pos -> new AltarSearchEntry(pos.immutable(), level.getBlockState(pos)))
            .filter(entry -> level.getBlockState(entry.pos).is(Registration.ALTAR_BLOCK))
            .sorted(Comparator.comparingDouble(entry -> entry.pos.distToCenterSqr(playerPos)))
            .toList();
        var altarPos = altarSearchEntries.stream()
            .filter(entry -> !entry.state.getValue(AltarBlock.ACTIVE))
            .map(AltarSearchEntry::pos)
            .findFirst()
            .orElseGet(() -> altarSearchEntries.isEmpty() ? null : altarSearchEntries.getFirst().pos);

        if (altarPos == null) {
            player.displayClientMessage(SummoningLang.PREVIEW_NO_ALTAR.get().withStyle(ChatFormatting.RED), true);
            return;
        }

        player.displayClientMessage(SummoningLang.PREVIEW_SUCCESS.get().withStyle(ChatFormatting.DARK_GREEN), true);
        INSTANCE.task = new Task(level.getGameTime(), altarPos, blockPatternCheck.getRenderPattern());
    }

    private record AltarSearchEntry(BlockPos pos, BlockState state) {}

    private record Task(long createdAt, BlockPos altarPos, List<ClientPatternEntry> pattern) {}
}
