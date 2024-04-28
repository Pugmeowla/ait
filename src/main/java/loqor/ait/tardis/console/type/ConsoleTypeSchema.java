package loqor.ait.tardis.console.type;

import com.google.gson.*;
import loqor.ait.registry.ConsoleRegistry;
import loqor.ait.registry.ConsoleVariantRegistry;
import loqor.ait.registry.datapack.Identifiable;
import loqor.ait.registry.datapack.Nameable;
import loqor.ait.registry.unlockable.Unlockable;
import loqor.ait.tardis.console.variant.ConsoleVariantSchema;
import loqor.ait.tardis.control.ControlTypes;
import loqor.ait.tardis.exterior.category.CapsuleCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import java.lang.reflect.Type;

public abstract class ConsoleTypeSchema implements Identifiable, Nameable {
	private final Identifier id;
	private final String name;

	protected ConsoleTypeSchema(Identifier id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;

		return o instanceof ConsoleTypeSchema schema
				&& id.equals(schema.id);
	}

	@Override
	public Identifier id() {
		return this.id;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public String toString() {
		return this.name();
	}

	//@TODO protocol abstraction with numbered letters

	public abstract ControlTypes[] getControlTypes(); // fixme this kinda sucks idk

	/**
	 * The default console for this category
	 */
	public ConsoleVariantSchema getDefaultVariant() {
		return ConsoleVariantRegistry.withParentToList(this).get(0);
	}

	public static Object serializer() {
		return new Serializer();
	}

	private static class Serializer implements JsonSerializer<ConsoleTypeSchema>, JsonDeserializer<ConsoleTypeSchema> {

		@Override
		public ConsoleTypeSchema deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			Identifier id;

			try {
				id = new Identifier(json.getAsJsonPrimitive().getAsString());
			} catch (InvalidIdentifierException e) {
				id = CapsuleCategory.REFERENCE;
			}

			return ConsoleRegistry.REGISTRY.get(id);
		}

		@Override
		public JsonElement serialize(ConsoleTypeSchema src, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(src.id().toString());
		}
	}
}
