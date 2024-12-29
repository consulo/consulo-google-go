package consulo.google.go.language.psi;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.stub.ObjectStubSerializerProvider;
import consulo.language.psi.stub.StubElementTypeHolder;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author VISTALL
 * @since 07-Aug-22
 */
@ExtensionImpl
public class GoTypesStubElementHolder extends StubElementTypeHolder<GoStubElementTypes> {
  @Nullable
  @Override
  public String getExternalIdPrefix() {
    return "go.";
  }

  @Nonnull
  @Override
  public List<ObjectStubSerializerProvider> loadSerializers() {
    return allFromStaticFields(GoStubElementTypes.class, Field::get);
  }
}
