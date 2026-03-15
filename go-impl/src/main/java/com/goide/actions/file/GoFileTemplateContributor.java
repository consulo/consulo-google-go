package com.goide.actions.file;

import consulo.annotation.component.ExtensionImpl;
import consulo.fileTemplate.FileTemplateContributor;
import consulo.fileTemplate.FileTemplateRegistrator;


/**
 * @author VISTALL
 * @since 07-Aug-22
 */
@ExtensionImpl
public class GoFileTemplateContributor implements FileTemplateContributor {
  @Override
  public void register(FileTemplateRegistrator registrator) {
    registrator.registerInternalTemplate("Go Application");
    registrator.registerInternalTemplate("Go File");
  }
}
