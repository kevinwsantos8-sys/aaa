package com.shadowkatana.katana;

import com.shadowkatana.ModSounds;
import com.shadowkatana.item.ModItems;
import com.shadowkatana.network.Payloads;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Toda a logica do servidor: sacar/guardar, poderes e upgrades. Constantes de balanceamento no topo. */
public final class KatanaServer {
    private static final double TP_RANGE = 12.0;
    private static final int TP_COOLDOWN = 60;       // 3 s
    private static final int INVIS_TICKS = 400;      // 20 s
    private static final int INVIS_COOLDOWN = 800;   // 40 s

    private static final String[] NAME_KEYS = {
            "screen.shadowkatana.sword", "screen.shadowkatana.teleport", "screen.shadowkatana.shadow"};

    private static final class S {
        int phase = KatanaPhase.NONE;
        long start;
        int prevSlot;
        boolean armed;
        boolean dirty = true;
        boolean cdSent;
        long tpReady;
        long shadowReady;
    }

    private static final Map<UUID, S> STATES = new HashMap<>();

    private static S state(ServerPlayerEntity p) {
        return STATES.computeIfAbsent(p.getUuid(), k -> new S());
    }

    // ------------------------------------------------------------------ entrada de pacotes

    public static void handleAction(ServerPlayerEntity p, int id) {
        S s = state(p);
        long now = p.getWorld().getTime();
        switch (id) {
            case 0 -> toggleDraw(p, s, now);
            case 1 -> teleport(p, s, now);
            case 2 -> invisibility(p, s, now);
            case 3, 4, 5 -> upgrade(p, id - 3);
            default -> { }
        }
    }

    // ------------------------------------------------------------------ sacar / guardar

    private static void toggleDraw(ServerPlayerEntity p, S s, long now) {
        switch (s.phase) {
            case KatanaPhase.SHEATHED -> {
                if (findHotbarSlot(p) < 0) {
                    p.sendMessage(Text.translatable("message.shadowkatana.not_in_hotbar"), true);
                    return;
                }
                s.prevSlot = p.getInventory().selectedSlot;
                setPhase(s, KatanaPhase.DRAWING, now);
            }
            case KatanaPhase.DRAWN -> {
                int current = p.getInventory().selectedSlot;
                int prev = s.prevSlot;
                if (prev == current || prev < 0 || prev > 8) prev = (current + 1) % 9;
                selectSlot(p, prev);
                setPhase(s, KatanaPhase.SHEATHING, now);
                play(p, ModSounds.KATANA_SWISH, 0.6f, 0.8f);
            }
            case KatanaPhase.NONE -> p.sendMessage(Text.translatable("message.shadowkatana.no_katana"), true);
            default -> { }
        }
    }

    public static void tick(ServerPlayerEntity p) {
        S s = state(p);
        long now = p.getWorld().getTime();
        long el = now - s.start;
        boolean has = KatanaData.findKatana(p) != null;
        boolean holding = p.getMainHandStack().isOf(ModItems.SHADOW_KATANA);

        switch (s.phase) {
            case KatanaPhase.NONE -> {
                if (has) setPhase(s, holding ? KatanaPhase.DRAWN : KatanaPhase.SHEATHED, now);
            }
            case KatanaPhase.SHEATHED -> {
                if (!has) setPhase(s, KatanaPhase.NONE, now);
                else if (holding) setPhase(s, KatanaPhase.DRAWN, now);
            }
            case KatanaPhase.DRAWING -> {
                if (el == 4) play(p, ModSounds.KATANA_DRAW, 1.0f, 1.0f);   // lamina sai das costas
                if (el == 9) play(p, ModSounds.KATANA_SWISH, 0.9f, 1.0f);  // corte no ar
                if (!has) {
                    setPhase(s, KatanaPhase.NONE, now);
                } else if (el >= KatanaPhase.DRAW_TICKS) {
                    int slot = findHotbarSlot(p);
                    if (slot >= 0) {
                        selectSlot(p, slot);
                        setPhase(s, KatanaPhase.DRAWN, now);
                    } else {
                        setPhase(s, KatanaPhase.SHEATHED, now);
                    }
                }
            }
            case KatanaPhase.DRAWN -> {
                if (!has) setPhase(s, KatanaPhase.NONE, now);
                else if (!holding) setPhase(s, KatanaPhase.SHEATHED, now);
            }
            case KatanaPhase.SHEATHING -> {
                if (el == 9) play(p, ModSounds.KATANA_SHEATHE, 1.0f, 1.0f); // encaixa nas costas
                if (!has) setPhase(s, KatanaPhase.NONE, now);
                else if (el >= KatanaPhase.SHEATH_TICKS) setPhase(s, KatanaPhase.SHEATHED, now);
            }
            default -> { }
        }

        if (s.armed && !p.hasStatusEffect(StatusEffects.INVISIBILITY)) s.armed = false;

        // manda a fase para o dono e para quem esta vendo (e repete a cada 1 s para quem acabou de chegar perto)
        if (s.dirty || now % 20 == 0) {
            Payloads.Phase payload = new Payloads.Phase(p.getId(), s.phase);
            ServerPlayNetworking.send(p, payload);
            for (ServerPlayerEntity other : PlayerLookup.tracking(p)) {
                ServerPlayNetworking.send(other, payload);
            }
            s.dirty = false;
        }

        // recargas para o HUD do dono
        int tpRem = (int) Math.max(0, s.tpReady - now);
        int shRem = (int) Math.max(0, s.shadowReady - now);
        boolean any = tpRem > 0 || shRem > 0;
        if (now % 5 == 0 && (any || s.cdSent)) {
            ServerPlayNetworking.send(p, new Payloads.Cooldown(tpRem, shRem));
            s.cdSent = any;
        }
    }

    private static void setPhase(S s, int phase, long now) {
        s.phase = phase;
        s.start = now;
        s.dirty = true;
    }

    private static void selectSlot(ServerPlayerEntity p, int slot) {
        p.getInventory().selectedSlot = slot;
        p.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(slot));
    }

    private static int findHotbarSlot(ServerPlayerEntity p) {
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).isOf(ModItems.SHADOW_KATANA)) return i;
        }
        return -1;
    }

    private static void play(ServerPlayerEntity p, SoundEvent sound, float volume, float pitch) {
        p.getWorld().playSound(null, p.getBlockPos(), sound, SoundCategory.PLAYERS, volume, pitch);
    }

    // ------------------------------------------------------------------ poderes

    /** Confere se pode usar um poder (katana na mao e sem recarga). */
    private static boolean canUse(ServerPlayerEntity p, S s, long now, long readyAt) {
        if (s.phase != KatanaPhase.DRAWN) {
            p.sendMessage(Text.translatable("message.shadowkatana.draw_first"), true);
            return false;
        }
        if (now < readyAt) {
            int secs = (int) ((readyAt - now + 19) / 20);
            p.sendMessage(Text.translatable("message.shadowkatana.cooldown", secs), true);
            return false;
        }
        return true;
    }

    private static void teleport(ServerPlayerEntity p, S s, long now) {
        if (!canUse(p, s, now, s.tpReady)) return;

        ServerWorld w = (ServerWorld) p.getWorld();
        Vec3d eye = p.getEyePos();
        Vec3d dir = p.getRotationVec(1.0f);
        Vec3d end = eye.add(dir.multiply(TP_RANGE));
        BlockHitResult hit = w.raycast(new RaycastContext(
                eye, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, p));
        Vec3d target = hit.getType() == HitResult.Type.MISS ? end : hit.getPos();

        double eyeOffset = p.getEyeY() - p.getY();
        Vec3d dest = null;
        for (int i = 0; i <= 24; i++) { // recua da parede ate achar espaco livre
            Vec3d c = target.subtract(dir.multiply(i * 0.5));
            double fx = c.x, fy = c.y - eyeOffset, fz = c.z;
            Box box = p.getBoundingBox().offset(fx - p.getX(), fy - p.getY(), fz - p.getZ());
            if (w.isSpaceEmpty(p, box)) {
                dest = new Vec3d(fx, fy, fz);
                break;
            }
        }
        if (dest == null || dest.distanceTo(p.getPos()) < 1.5) {
            p.sendMessage(Text.translatable("message.shadowkatana.teleport_blocked"), true);
            return;
        }

        ItemStack katana = KatanaData.findKatana(p);
        double damage = katana == null ? 2.0 : KatanaData.teleportDamage(KatanaData.getLevel(katana, KatanaData.TELEPORT));

        Vec3d from = p.getPos();
        w.spawnParticles(ParticleTypes.SQUID_INK, from.x, from.y + 1.0, from.z, 30, 0.4, 0.8, 0.4, 0.05);
        w.spawnParticles(ParticleTypes.PORTAL, from.x, from.y + 1.0, from.z, 30, 0.4, 0.8, 0.4, 0.3);

        // dano em quem esta no caminho / destino
        List<LivingEntity> hits = w.getEntitiesByClass(LivingEntity.class,
                new Box(from, dest).expand(1.5), e -> e != p && e.isAlive());
        for (LivingEntity e : hits) {
            e.damage(w.getDamageSources().playerAttack(p), (float) damage);
        }

        p.networkHandler.requestTeleport(dest.x, dest.y, dest.z, p.getYaw(), p.getPitch());
        p.fallDistance = 0;
        w.spawnParticles(ParticleTypes.SQUID_INK, dest.x, dest.y + 1.0, dest.z, 30, 0.4, 0.8, 0.4, 0.05);
        w.spawnParticles(ParticleTypes.SMOKE, dest.x, dest.y + 1.0, dest.z, 20, 0.4, 0.8, 0.4, 0.02);
        play(p, ModSounds.KATANA_SWISH, 1.0f, 0.6f);
        s.tpReady = now + TP_COOLDOWN;
    }

    private static void invisibility(ServerPlayerEntity p, S s, long now) {
        if (!canUse(p, s, now, s.shadowReady)) return;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.INVISIBILITY, INVIS_TICKS, 0, false, false, true));
        s.armed = true;
        s.shadowReady = now + INVIS_COOLDOWN;
        ServerWorld w = (ServerWorld) p.getWorld();
        w.spawnParticles(ParticleTypes.SQUID_INK, p.getX(), p.getY() + 1.0, p.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
        play(p, ModSounds.KATANA_SWISH, 0.8f, 0.5f);
        p.sendMessage(Text.translatable("message.shadowkatana.invisible_on", INVIS_TICKS / 20), true);
    }

    /** Chamado pela katana depois de acertar um golpe corpo a corpo. */
    public static void onHit(ServerPlayerEntity p, LivingEntity target, ItemStack stack) {
        S s = state(p);
        double bonus = KatanaData.swordBonus(KatanaData.getLevel(stack, KatanaData.SWORD));
        if (s.armed) {
            bonus += KatanaData.shadowBonus(KatanaData.getLevel(stack, KatanaData.SHADOW));
            s.armed = false;
            ServerWorld w = (ServerWorld) p.getWorld();
            w.spawnParticles(ParticleTypes.SQUID_INK, target.getX(), target.getY() + 1.0, target.getZ(), 25, 0.3, 0.6, 0.3, 0.1);
        }
        if (bonus > 0 && target.isAlive()) {
            target.timeUntilRegen = 0; // deixa o dano extra passar logo depois do golpe
            target.damage(p.getWorld().getDamageSources().playerAttack(p), (float) bonus);
        }
    }

    // ------------------------------------------------------------------ upgrades

    private static void upgrade(ServerPlayerEntity p, int type) {
        ItemStack katana = KatanaData.findKatana(p);
        if (katana == null) {
            p.sendMessage(Text.translatable("message.shadowkatana.no_katana"), true);
            return;
        }
        int level = KatanaData.getLevel(katana, type);
        if (level >= KatanaData.MAX_LEVEL) return;
        int cost = KatanaData.cost(level);
        if (!p.isCreative()) {
            if (p.experienceLevel < cost) {
                p.sendMessage(Text.translatable("message.shadowkatana.not_enough_xp", cost), true);
                return;
            }
            p.addExperienceLevels(-cost);
        }
        KatanaData.setLevel(katana, type, level + 1);
        play(p, ModSounds.KATANA_DRAW, 0.6f, 1.6f);
        p.sendMessage(Text.translatable("message.shadowkatana.upgraded", Text.translatable(NAME_KEYS[type]), level + 1), true);
    }

    private KatanaServer() {}
}
