package com.goide.inspections;

import consulo.configurable.ConfigurableBuilder;
import consulo.configurable.ConfigurableBuilderState;
import consulo.configurable.UnnamedConfigurable;
import consulo.language.editor.inspection.InspectionToolState;
import consulo.localize.LocalizeValue;
import consulo.util.xml.serializer.XmlSerializerUtil;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 22/03/2023
 */
public class GoStructInitializationInspectionState implements InspectionToolState<GoStructInitializationInspectionState> {
  public boolean reportLocalStructs;

  @Nullable
  @Override
  public UnnamedConfigurable createConfigurable() {
    ConfigurableBuilder<ConfigurableBuilderState> builder = ConfigurableBuilder.newBuilder();
    builder.checkBox(LocalizeValue.localizeTODO("Report for local type definitions as well"),
                     () -> reportLocalStructs,
                     b -> reportLocalStructs = b);
    return builder.buildUnnamed();
  }

  @Nullable
  @Override
  public GoStructInitializationInspectionState getState() {
    return this;
  }

  @Override
  public void loadState(GoStructInitializationInspectionState state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
