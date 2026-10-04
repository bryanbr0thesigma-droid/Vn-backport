package com.backport.client;

import com.backport.Backport;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Draws the 26.x locator bar in place of the experience bar while other players are being tracked. */
public final class LocatorBarClient {
   private static final ResourceLocation SHEET = Backport.id("textures/gui/locator_bar.png");
   private static final int NEAR = 128;
   private static final int FAR = 332;
   private static boolean enabled;
   private static List<Entry> entries = List.of();

   private record Entry(UUID id, Vec3 pos, int color) {
   }

   private LocatorBarClient() {
   }

   public static void receive(FriendlyByteBuf buf) {
      boolean on = buf.readBoolean();
      int n = buf.readVarInt();
      List<Entry> list = new ArrayList<>(n);
      for (int i = 0; i < n; i++) {
         UUID id = buf.readUUID();
         Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
         int color = buf.readInt();
         list.add(new Entry(id, pos, color));
      }
      Minecraft.getInstance().execute(() -> {
         enabled = on;
         entries = list;
      });
   }

   public static void clear() {
      enabled = false;
      entries = List.of();
   }

   public static boolean active() {
      return enabled && !entries.isEmpty();
   }

   private static int colorFor(Entry e) {
      if (e.color >= 0) {
         return 0xFF000000 | e.color;
      }
      int rgb = e.id.hashCode() & 0xFFFFFF;
      float[] hsb = java.awt.Color.RGBtoHSB(rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, null);
      return 0xFF000000 | (java.awt.Color.HSBtoRGB(hsb[0], hsb[1], 0.9F) & 0xFFFFFF);
   }

   public static void render(GuiGraphics g, float partial) {
      Minecraft mc = Minecraft.getInstance();
      Camera camera = mc.gameRenderer.getMainCamera();
      Player self = mc.player;
      if (self == null || mc.level == null) {
         return;
      }
      int width = g.guiWidth();
      int left = width / 2 - 91;
      int top = g.guiHeight() - 29;
      g.blit(SHEET, left, top, 5, 5, 0.0F, 0.0F, 5, 5, 64, 32);
      g.blit(SHEET, left + 5, top, 172, 5, 5.0F, 0.0F, 2, 5, 64, 32);
      g.blit(SHEET, left + 177, top, 5, 5, 7.0F, 0.0F, 5, 5, 64, 32);
      Vec3 cam = camera.getPosition();
      float yaw = camera.getYRot();
      float vFov = (float) mc.options.fov().get().doubleValue();
      for (Entry e : entries) {
         Player other = mc.level.getPlayerByUUID(e.id);
         Vec3 pos = other != null ? other.getPosition(partial).add(0.0, other.getBbHeight() * 0.5, 0.0) : e.pos;
         Vec3 dir = cam.subtract(pos);
         Vec3 rot = new Vec3(-dir.z, dir.y, dir.x);
         double angle = Mth.wrapDegrees(Math.toDegrees(Mth.atan2(rot.z, rot.x)) - yaw);
         if (mc.level.getGameTime() % 40 == 0) {
            Backport.LOGGER.info("[locator-debug] angle={} yaw={} dist={} loaded={}", angle, yaw, cam.distanceTo(pos), other != null);
         }
         if (angle <= -60.0 || angle > 60.0) {
            continue;
         }
         float dist = (float) cam.distanceTo(pos);
         int sprite = dist < NEAR ? 0 : dist >= FAR ? 3 : Mth.lerpInt((dist - NEAR) / (FAR - NEAR), 1, 3);
         int dotX = Mth.ceil((width - 9) / 2.0F) + Mth.floor(angle * 173.0 / 2.0 / 60.0);
         int argb = colorFor(e);
         RenderSystem.enableBlend();
         RenderSystem.setShaderColor((argb >> 16 & 255) / 255.0F, (argb >> 8 & 255) / 255.0F, (argb & 255) / 255.0F, 1.0F);
         g.blit(SHEET, dotX, top - 2, sprite * 9, 8, 9, 9, 64, 32);
         RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
         double horiz = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
         double pitchToTarget = Math.toDegrees(Math.atan2(-(pos.y - cam.y), Math.max(horiz, 1.0E-4)));
         double delta = pitchToTarget - camera.getXRot();
         int frame = mc.level.getGameTime() % 14 < 10 ? 0 : 5;
         if (delta < -vFov * 0.5) {
            g.blit(SHEET, dotX + 1, top - 6, 40, 8 + frame, 7, 5, 64, 32);
         } else if (delta > vFov * 0.5) {
            g.blit(SHEET, dotX + 1, top + 6, 48, 8 + frame, 7, 5, 64, 32);
         }
      }
   }
}
