package online.remind.remind.reactioncommands;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import net.minecraft.world.phys.Vec3;
import online.kingdomkeys.kingdomkeys.ability.ModAbilities;
import online.kingdomkeys.kingdomkeys.data.PlayerData;
import online.kingdomkeys.kingdomkeys.data.WorldData;
import online.kingdomkeys.kingdomkeys.driveform.DriveForm;
import online.kingdomkeys.kingdomkeys.driveform.ModDriveForms;
import online.kingdomkeys.kingdomkeys.effects.ModMobEffects;
import online.kingdomkeys.kingdomkeys.lib.Party;
import online.kingdomkeys.kingdomkeys.lib.Strings;
import online.kingdomkeys.kingdomkeys.reactioncommands.ReactionCommand;
import online.kingdomkeys.kingdomkeys.damagesource.KKDamageTypes;

import online.remind.remind.KingdomKeysReMind;
import online.remind.remind.ability.ModAbilitiesRM;
import online.remind.remind.capabilities.GlobalDataRM;
import online.remind.remind.capabilities.ModDataRM;
import online.remind.remind.client.gui.FF7AttackHud;
import online.remind.remind.client.sound.ModSoundsRM;
import online.remind.remind.lib.StringsRM;
import online.remind.remind.network.PacketHandlerRM;
import online.remind.remind.styles.data.StyleDefinition;
import online.remind.remind.styles.data.StyleRegistry;

import java.util.Comparator;
import java.util.List;

public class StyleRC extends ReactionCommand {

	private final String type; // DriveForm ID (e.g. "kkremind:form_firestorm")

	public StyleRC(ResourceLocation registryName, boolean constantCheck, String type) {
		super(registryName, constantCheck, 20 * 20, 0xff6f00);
		this.type = type;
	}

	// ------------------------------------------------------------
	// MAIN RC LOGIC
	// ------------------------------------------------------------
	@Override
	public void onUse(Player player, LivingEntity target, LivingEntity ignored) {

		if (!conditionsToAppear(player, player))
			return;

		PlayerData playerData = PlayerData.get(player);
		GlobalDataRM remindData = ModDataRM.getGlobal(player);

		// ------------------------------------------------------------
		// 1. ACTIVATE STYLE (not in this Style yet)
		// ------------------------------------------------------------
		if (!playerData.getActiveDriveForm().equals(ResourceLocation.parse(type))) {

			DriveForm form = ModDriveForms.registry.get(ResourceLocation.parse(type));
			if (form != null) {
				form.initDrive(player);
				//System.out.println("Entered Style. Active Form is now: " + playerData.getActiveDriveForm());
			}

			// Reset SGauge + Style state
			remindData.setSituationValue(0);
			remindData.setStyle("NONE");

			StyleDefinition def = StyleRegistry.getStyleForDriveForm(ResourceLocation.parse(type));
			remindData.setStyleTicks(100 + (10 * playerData.getNumberOfAbilitiesEquipped(ModAbilitiesRM.FORM_BOOST)));
			System.out.println(remindData.getStyleTicks());

			PacketHandlerRM.syncGlobalToAllAround(player, remindData);

			// Remove RC after activation
			playerData.removeReactionCommand(getRegistryName());
			return;
		}

		// ------------------------------------------------------------
		// 2. FINISHER (already in this Style)
		// ------------------------------------------------------------
		useStyleFinisher(player);

		// Exit Style
		playerData.addFP(-1000);
		remindData.setSituationValue(0);
		remindData.setStyle("NONE");
		remindData.setStyleTicks(0);

		PacketHandlerRM.syncGlobalToAllAround(player, remindData);
	}

	// ------------------------------------------------------------
	// FINISHER LOGIC (Unified for all Styles)
	// ------------------------------------------------------------
	private void useStyleFinisher(Player player) {
		PlayerData playerData = PlayerData.get(player);
		float damage = (playerData.getMagic(true) + playerData.getStrength(true)) / 2f;

		switch (type) {

			case KingdomKeysReMind.MODID + ":" + StringsRM.fireStorm -> {
				float mult = playerData.getNumberOfAbilitiesEquipped(ModAbilities.FIRE_BOOST) * 0.25F;
				final float finisherDamage = damage * (1.0F + mult);

				MinecraftServer server = player.getServer();

				// BBS: leap upward and charge the fire attack
				player.setDeltaMovement(
						player.getDeltaMovement().x,
						0.65D,
						player.getDeltaMovement().z
				);
				player.hasImpulse = true;

				playSoundAndParticles(
						player,
						SoundEvents.BLAZE_SHOOT,
						ParticleTypes.FLAME,
						ParticleTypes.SMALL_FLAME
				);

				if (server != null) {
					// BBS finisher produces multiple flame pillars.
					// Split the damage between them instead of doing full damage 3x.
					for (int i = 0; i < 3; i++) {
						final int pillar = i;

						server.tell(new TickTask(
								server.getTickCount() + 6 + (pillar * 2),
								() -> {
									if (!player.isAlive()) {
										return;
									}

									explosionHurt(
											player,
											finisherDamage / 3.0F,
											KKDamageTypes.FIRE
									);

									playSoundAndParticles(
											player,
											SoundEvents.FIRECHARGE_USE,
											ParticleTypes.FLAME,
											ParticleTypes.LAVA,
											ParticleTypes.SMALL_FLAME
									);
								}
						));
					}
				}
			}

			case KingdomKeysReMind.MODID + ":" + StringsRM.diamondDust -> {
				float mult = playerData.getNumberOfAbilitiesEquipped(ModAbilities.BLIZZARD_BOOST) * 0.25F;
				final float finisherDamage = damage * (1.0F + mult);

				MinecraftServer server = player.getServer();

				// Glacier forming
				playSoundAndParticles(
						player,
						SoundEvents.POWDER_SNOW_PLACE,
						ParticleTypes.SNOWFLAKE,
						ParticleTypes.ITEM_SNOWBALL
				);

				if (server != null) {
					server.tell(new TickTask(
							server.getTickCount() + 6,
							() -> {
								if (!player.isAlive()) {
									return;
								}

								// BBS: giant glacier bursts around the user
								explosionHurt(
										player,
										finisherDamage,
										KKDamageTypes.ICE
								);

								playSoundAndParticles(
										player,
										SoundEvents.GLASS_BREAK,
										ParticleTypes.SNOWFLAKE,
										ParticleTypes.ITEM_SNOWBALL,
										ParticleTypes.CLOUD
								);
							}
					));
				}
			}

			case KingdomKeysReMind.MODID + ":" + StringsRM.thunderBolt -> {
				float mult = playerData.getNumberOfAbilitiesEquipped(ModAbilities.THUNDER_BOOST) * 0.25F;
				final float finisherDamage = damage * (1.0F + mult);

				MinecraftServer server = player.getServer();

				// BBS: raise Keyblade and form the electric orb
				playSoundAndParticles(
						player,
						SoundEvents.BEACON_POWER_SELECT,
						ParticleTypes.ELECTRIC_SPARK,
						ParticleTypes.END_ROD
				);

				if (server != null) {
					// Three raining lightning strikes
					for (int i = 0; i < 3; i++) {
						final int strike = i;

						server.tell(new TickTask(
								server.getTickCount() + 4 + (strike * 3),
								() -> {
									if (!player.isAlive()) {
										return;
									}

									explosionHurt(
											player,
											finisherDamage / 3.0F,
											KKDamageTypes.LIGHTNING
									);

									playSoundAndParticles(
											player,
											SoundEvents.LIGHTNING_BOLT_IMPACT,
											ParticleTypes.ELECTRIC_SPARK,
											ParticleTypes.END_ROD
									);
								}
						));
					}
				}
			}

			case KingdomKeysReMind.MODID + ":" + StringsRM.feverPitch -> {
				int atkHaste = playerData.getNumberOfAbilitiesEquipped(
						ModAbilitiesRM.ATTACK_HASTE
				);

				/*
				 * For Fever Pitch I would NOT make Attack Haste add 25% damage.
				 * Make it speed up the finisher instead.
				 */
				final int spacing = Math.max(
						1,
						3 - Math.min(atkHaste, 2)
				);

				final float finisherDamage = damage;

				MinecraftServer server = player.getServer();

				Vec3 look = player.getLookAngle().normalize();

				// Initial forward rush
				player.setDeltaMovement(
						look.x * 0.45D,
						player.getDeltaMovement().y,
						look.z * 0.45D
				);
				player.hasImpulse = true;

				if (server != null) {

					// BBS: four rapid forward strikes
					for (int i = 0; i < 4; i++) {
						final int hit = i;

						server.tell(new TickTask(
								server.getTickCount() + (hit * spacing),
								() -> {
									if (!player.isAlive()) {
										return;
									}

									explosionHurt(
											player,
											finisherDamage * 0.15F,
											KKDamageTypes.OFFHAND
									);

									playSoundAndParticles(
											player,
											SoundEvents.PLAYER_ATTACK_SWEEP,
											ParticleTypes.SWEEP_ATTACK,
											ParticleTypes.CRIT
									);
								}
						));
					}

					// Final light-slash portion
					server.tell(new TickTask(
							server.getTickCount() + (4 * spacing) + 1,
							() -> {
								if (!player.isAlive()) {
									return;
								}

								explosionHurt(
										player,
										finisherDamage * 0.40F,
										KKDamageTypes.OFFHAND
								);

								playSoundAndParticles(
										player,
										SoundEvents.PLAYER_ATTACK_STRONG,
										ParticleTypes.SWEEP_ATTACK,
										ParticleTypes.END_ROD,
										ParticleTypes.CRIT
								);
							}
					));
				}
			}

			case KingdomKeysReMind.MODID + ":" + StringsRM.criticalImpact -> {
				float mult = playerData.getNumberOfAbilitiesEquipped(
						ModAbilities.CRITICAL_BOOST
				) * 0.25F;

				final float finisherDamage = damage * (1.0F + mult);

				MinecraftServer server = player.getServer();

				// BBS: jump upward first
				player.setDeltaMovement(
						player.getDeltaMovement().x,
						0.80D,
						player.getDeltaMovement().z
				);
				player.hasImpulse = true;

				if (server != null) {

					// Start the downward slam
					server.tell(new TickTask(
							server.getTickCount() + 5,
							() -> {
								if (!player.isAlive()) {
									return;
								}

								player.setDeltaMovement(
										0.0D,
										-1.25D,
										0.0D
								);
								player.hasImpulse = true;
							}
					));

					// Ground impact
					server.tell(new TickTask(
							server.getTickCount() + 8,
							() -> {
								if (!player.isAlive()) {
									return;
								}

								player.fallDistance = 0.0F;

								explosionHurt(
										player,
										finisherDamage,
										KKDamageTypes.OFFHAND
								);

								playSoundAndParticles(
										player,
										SoundEvents.GENERIC_EXPLODE.value(),
										ParticleTypes.EXPLOSION,
										ParticleTypes.CRIT,
										ParticleTypes.CAMPFIRE_COSY_SMOKE
								);
							}
					));
				}
			}

			case KingdomKeysReMind.MODID + ":" + StringsRM.spellweaver -> {
				final float finisherDamage = damage;

				MinecraftServer server = player.getServer();

				playSoundAndParticles(
						player,
						SoundEvents.EVOKER_CAST_SPELL,
						ParticleTypes.ENCHANT,
						ParticleTypes.END_ROD
				);

				if (server != null) {

					/*
					 * Actual BBS Spellweaver is listed as 28 hits.
					 *
					 * Minecraft runs at 20 TPS, so one pulse every tick gives us
					 * a ~1.4 second magical spinning barrage.
					 *
					 * Player isn't rooted, so they can move during it like the
					 * BBS left-stick-controlled finisher.
					 */
					for (int i = 0; i < 28; i++) {
						final int hit = i;

						server.tell(new TickTask(
								server.getTickCount() + hit,
								() -> {
									if (!player.isAlive()) {
										return;
									}

									explosionHurt(
											player,
											finisherDamage / 28.0F,
											KKDamageTypes.OFFHAND
									);

									// Don't spam the sound 28 times
									if (hit % 4 == 0) {
										playSoundAndParticles(
												player,
												SoundEvents.AMETHYST_BLOCK_CHIME,
												ParticleTypes.ENCHANT,
												ParticleTypes.END_ROD
										);
									}
								}
						));
					}

					server.tell(new TickTask(
							server.getTickCount() + 28,
							() -> {
								if (!player.isAlive()) {
									return;
								}

								playSoundAndParticles(
										player,
										SoundEvents.EVOKER_CAST_SPELL,
										ParticleTypes.END_ROD,
										ParticleTypes.ENCHANT
								);
							}
					));
				}
			}

			case KingdomKeysReMind.MODID + ":" + StringsRM.darkImpulse -> {

				// Dark Boost scaling
				float mult = playerData.getNumberOfAbilitiesEquipped(ModAbilitiesRM.DARKNESS_BOOST) * 0.25F;

				damage += damage * mult;

				final float darkImpulseDamage = damage;
				// Find the nearest enemy to burst underneath
				LivingEntity target = player.level()
						.getEntitiesOfClass(
								LivingEntity.class,
								player.getBoundingBox().inflate(12.0D),
								entity ->
										entity != player
												&& entity.isAlive()
												&& !(entity instanceof Player)
						)
						.stream()
						.min(Comparator.comparingDouble(player::distanceToSqr))
						.orElse(null);

				if (target == null) {
					// No target: just perform the burst where the player is
					explosionHurt(player, darkImpulseDamage, KKDamageTypes.DARKNESS);
					playSoundAndParticles(player, SoundEvents.ENDERMAN_TELEPORT, ParticleTypes.SMOKE);
					break;
				}

				MinecraftServer server = player.getServer();

				if (server == null) {
					break;
				}

				// Prevent the player from colliding while "underground"
				player.noPhysics = true;
				player.setInvisible(true);

				// Sink
				player.setDeltaMovement(0.0D, -0.25D, 0.0D);

				playSoundAndParticles(
						player,
						SoundEvents.ENDERMAN_TELEPORT,
						ParticleTypes.SMOKE
				);

				// Move underneath the target after a short delay
				server.tell(new TickTask(
						server.getTickCount() + 4,
						() -> {
							if (!player.isAlive() || !target.isAlive()) {
								player.noPhysics = false;
								player.setInvisible(false);
								return;
							}

							player.teleportTo(
									target.getX(),
									target.getY() - 1.0D,
									target.getZ()
							);

							// Burst out shortly afterward
							server.tell(new TickTask(
									server.getTickCount() + 3,
									() -> {
										if (!player.isAlive()) {
											return;
										}

										player.teleportTo(
												target.getX(),
												target.getY(),
												target.getZ()
										);

										player.noPhysics = false;
										player.setInvisible(false);

										// Upward uppercut motion
										player.setDeltaMovement(
												0.0D,
												0.85D,
												0.0D
										);

										player.hasImpulse = true;

										explosionHurt(
												player,
												darkImpulseDamage,
												KKDamageTypes.DARKNESS
										);
									}
							));
						}
				));
			}

			case KingdomKeysReMind.MODID + ":" + StringsRM.bloodlust -> {
				int darkBoosts = playerData.getNumberOfAbilitiesEquipped(ModAbilitiesRM.DARKNESS_BOOST);

				float mult = darkBoosts * 0.25F;
				float finalDamage = damage + (damage * mult);

				float healAmount = Math.min(finalDamage * 0.25F, 8.0F);
				player.heal(healAmount);

				explosionHurt(player, finalDamage, KKDamageTypes.DARKNESS);

				playSoundAndParticles(
						player,
						SoundEvents.EVOKER_CAST_SPELL,
						ParticleTypes.DAMAGE_INDICATOR
				);

				if (player.level() instanceof ServerLevel serverLevel) {
					serverLevel.sendParticles(
							ParticleTypes.DAMAGE_INDICATOR,
							player.getX(),
							player.getY() + 1.0D,
							player.getZ(),
							30,
							1.0D,
							0.6D,
							1.0D,
							0.08D
					);

					serverLevel.sendParticles(
							ParticleTypes.SOUL,
							player.getX(),
							player.getY() + 1.0D,
							player.getZ(),
							20,
							0.8D,
							0.5D,
							0.8D,
							0.05D
					);

					serverLevel.sendParticles(
							ParticleTypes.WITCH,
							player.getX(),
							player.getY() + 1.0D,
							player.getZ(),
							18,
							0.7D,
							0.5D,
							0.7D,
							0.03D
					);
				}

				player.level().playSound(
						null,
						player.blockPosition(),
						SoundEvents.WITHER_AMBIENT,
						SoundSource.PLAYERS,
						0.8F,
						1.25F
				);
			}

			case KingdomKeysReMind.MODID + ":" + StringsRM.exSoldier -> {

				FF7AttackHud.show((ServerPlayer) player, "Cherry Blossom");
				player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSoundsRM.LIMIT_BREAK.get(), SoundSource.PLAYERS, 1F, 1F);


				/*
				 * CHERRY BLOSSOM
				 *
				 * Three enormous elemental impacts:
				 *
				 * 1. Thunder
				 * 2. Ice
				 * 3. Fire
				 *
				 * The damage calculation is captured now so changing state
				 * during the sequence doesn't alter the later hits.
				 */

				float mult =
						playerData.getNumberOfAbilitiesEquipped(
								ModAbilities.CRITICAL_BOOST
						) * 0.25F;

				damage +=
						damage * mult;

				final float cherryBlossomDamage =
						damage;


				if (!(player instanceof ServerPlayer serverPlayer)) {
					break;
				}


				var server =
						serverPlayer.getServer();

				int startTick =
						server.getTickCount();


				// ============================================================
				// HIT 1 - THUNDER
				// ============================================================

				server.tell(
						new TickTask(
								startTick + 1,
								() -> {

									if (!serverPlayer.isAlive()) {
										return;
									}

									explosionHurt(
											serverPlayer,
											cherryBlossomDamage,
											KKDamageTypes.LIGHTNING
									);

									playSoundAndParticles(
											serverPlayer,
											SoundEvents.LIGHTNING_BOLT_IMPACT,
											ParticleTypes.ELECTRIC_SPARK,
											ParticleTypes.FLASH
									);
								}
						)
				);


				// ============================================================
				// HIT 2 - ICE
				// ============================================================

				server.tell(
						new TickTask(
								startTick + 10,
								() -> {

									if (!serverPlayer.isAlive()) {
										return;
									}

									explosionHurt(
											serverPlayer,
											cherryBlossomDamage,
											KKDamageTypes.ICE
									);

									playSoundAndParticles(
											serverPlayer,
											SoundEvents.GLASS_BREAK,
											ParticleTypes.SNOWFLAKE,
											ParticleTypes.ITEM_SNOWBALL
									);
								}
						)
				);


				// ============================================================
				// HIT 3 - FIRE
				// ============================================================

				server.tell(
						new TickTask(
								startTick + 19,
								() -> {

									if (!serverPlayer.isAlive()) {
										return;
									}

									explosionHurt(
											serverPlayer,
											cherryBlossomDamage,
											KKDamageTypes.FIRE
									);

								}
						)
				);
			}
		}
	}

	// ------------------------------------------------------------
	// RC VISIBILITY LOGIC (Activation, Chain-up, Finisher)
	// ------------------------------------------------------------
	@Override
	public boolean conditionsToAppear(Player player, LivingEntity ignored) {

		PlayerData playerData = PlayerData.get(player);
		GlobalDataRM remindData = ModDataRM.getGlobal(player);

		if (playerData == null || remindData == null)
			return false;

		String style = remindData.getStyle();
		double gauge = remindData.getSituationValue();
		String driveId = type;

		/*System.out.println("\n=== StyleRC.conditionsToAppear() ===");
		System.out.println("RC Registry Name: " + getRegistryName());
		System.out.println("RC Type (this.type): " + type);
		System.out.println("Active Form: " + playerData.getActiveDriveForm());
		System.out.println("Gauge: " + gauge);
		System.out.println("Style String: " + style);*/

		// Finisher RC - CHECK THIS FIRST
		boolean isFinisher = playerData.getActiveDriveForm().equals(ResourceLocation.parse(driveId));
		//System.out.println("Finisher Check: activeForm.equals(type) = " + playerData.getActiveDriveForm() + ".equals(" + driveId + ") = " + isFinisher);
		if (isFinisher) {
			boolean result = isFinisher;
			//System.out.println("FINISHER MATCHED! gauge >= 100? " + result);
			return result;
		}

		// Activation RC
		boolean isNone = playerData.isFormActive(ModDriveForms.NONE);
		//System.out.println("Activation Check: activeForm == NONE? " + isNone);
		if (isNone) {
			boolean styleContainsCheck = styleContains(style, driveId);
			boolean result = styleContainsCheck;
			//boolean result = gauge >= 100 && styleContainsCheck;
			//System.out.println("ACTIVATION: gauge >= 100? " + (gauge >= 100) + ", styleContains? " + styleContainsCheck + ", Result: " + result);
			return result;
		}

		// Chain-up RC
		//System.out.println("Checking Chain-up...");
		if (!playerData.isFormActive(ModDriveForms.NONE)) {

			StyleDefinition current = StyleRegistry.getCurrentStyleDefinition(player);
			StyleDefinition target = StyleRegistry.getStyleForDriveForm(ResourceLocation.parse(driveId));

			//System.out.println("Current Style: " + (current != null ? current.target() : "null") + ", Level: " + (current != null ? current.styleLevel() : "N/A"));
			//System.out.println("Target Style: " + (target != null ? target.target() : "null") + ", Level: " + (target != null ? target.styleLevel() : "N/A"));

			if (current != null && target != null) {
				boolean isChainUp = target.styleLevel() == current.styleLevel() + 1;
				//System.out.println("isChainUp: " + target.styleLevel() + " == " + (current.styleLevel() + 1) + " = " + isChainUp);
				if (isChainUp) {
					boolean styleContainsCheck = styleContains(style, driveId);
					//boolean result = styleContainsCheck;
					boolean result = gauge >= 100 && styleContainsCheck;
					//System.out.println("CHAIN-UP: gauge >= 100? " + (gauge >= 100) + ", styleContains? " + styleContainsCheck + ", Result: " + result);
					return result;
				}
			}
		}

		//System.out.println("NO CONDITIONS MET - Returning FALSE");
		return false;
	}

	private boolean styleContains(String styleString, String styleId) {
		if (styleString == null || styleString.isEmpty())
			return false;

		for (String s : styleString.split(",")) {
			if (s.equals(styleId))
				return true;
		}
		return false;
	}

	// ------------------------------------------------------------
	// PARTICLES + SOUND HELPERS
	// ------------------------------------------------------------
	private void playSoundAndParticles(Player player, SoundEvent sound, SimpleParticleType... particles) {
		ServerLevel level = (ServerLevel) player.level();
		double X = player.getX(), Y = player.getY(), Z = player.getZ();

		double radius = 6;
		for (int t = 1; t < 360; t += 20) {
			for (int s = 1; s < 360; s += 20) {
				double x = X + (radius * Math.cos(Math.toRadians(s)) * Math.sin(Math.toRadians(t)));
				double z = Z + (radius * Math.sin(Math.toRadians(s)) * Math.sin(Math.toRadians(t)));
				double y = Y + (radius * Math.cos(Math.toRadians(t)));

				for (SimpleParticleType p : particles)
					level.sendParticles(p, x, y, z, 2, 0.05, 0.05, 0.05, 0.01);
			}
		}

		player.level().playSound(null, player.blockPosition(), sound, SoundSource.PLAYERS, 1F, 1F);
	}

	// ------------------------------------------------------------
	// DAMAGE HELPER
	// ------------------------------------------------------------
	public void explosionHurt(Player player, float damage, ResourceKey<DamageType> dmgType) {
		double radius = 6;
		List<LivingEntity> targets = player.level().getEntitiesOfClass(LivingEntity.class,
				player.getBoundingBox().inflate(radius));

		for (LivingEntity target : targets) {
			if (target == player)
				continue;

			Party p = WorldData.get(player.getServer()).getPartyFromMember(player.getUUID());
			boolean friendly = p != null && p.getMember(target.getUUID()) != null && !p.getFriendlyFire();

			if (!friendly) {
				target.hurt(KKDamageTypes.getElementalDamage(dmgType, player, player), damage);
				target.invulnerableTime = 0;

				if (dmgType == KKDamageTypes.FIRE)
					target.igniteForTicks(5);
				else if (dmgType == KKDamageTypes.ICE)
					target.addEffect(new MobEffectInstance(ModMobEffects.FREEZE, 80, 0));
			}
		}
	}
}
