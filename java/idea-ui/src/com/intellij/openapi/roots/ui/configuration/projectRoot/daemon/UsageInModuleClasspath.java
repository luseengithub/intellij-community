package com.intellij.openapi.roots.ui.configuration.projectRoot.daemon;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModuleRootModel;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.impl.OrderEntryUtil;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.openapi.roots.ui.configuration.projectRoot.StructureConfigurableContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author nik
 */
public class UsageInModuleClasspath extends ProjectStructureElementUsage {
  private final StructureConfigurableContext myContext;
  private final ModuleProjectStructureElement myContainingElement;
  private final ProjectStructureElement mySourceElement;
  private final Module myModule;

  public UsageInModuleClasspath(@NotNull StructureConfigurableContext context, @NotNull ModuleProjectStructureElement containingElement, ProjectStructureElement sourceElement) {
    myContext = context;
    myContainingElement = containingElement;
    myModule = containingElement.getModule();
    mySourceElement = sourceElement;
  }


  @Override
  public ProjectStructureElement getSourceElement() {
    return mySourceElement;
  }

  @Override
  public ModuleProjectStructureElement getContainingElement() {
    return myContainingElement;
  }

  public Module getModule() {
    return myModule;
  }

  @Override
  public String getPresentableName() {
    return myModule.getName();
  }

  @Override
  public void navigate() {
    ModulesConfigurator modulesConfigurator = myContext.getModulesConfigurator();

    ModuleRootModel rootModel = modulesConfigurator.getRootModel(myModule);
    OrderEntry entry;
    if (mySourceElement instanceof LibraryProjectStructureElement) {
      entry = OrderEntryUtil.findLibraryOrderEntry(rootModel, ((LibraryProjectStructureElement)mySourceElement).getLibrary());
    }
    else if (mySourceElement instanceof ModuleProjectStructureElement) {
      entry = OrderEntryUtil.findModuleOrderEntry(rootModel, ((ModuleProjectStructureElement)mySourceElement).getModule());
    }
    else if (mySourceElement instanceof SdkProjectStructureElement) {
      entry = OrderEntryUtil.findJdkOrderEntry(rootModel, ((SdkProjectStructureElement)mySourceElement).getSdk());
    }
    else {
      entry = null;
    }
    ProjectStructureConfigurable.getInstance(myContext.getProject()).selectOrderEntry(myModule, entry);
  }

  @Override
  public int hashCode() {
    return myModule.hashCode()*31 + mySourceElement.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof UsageInModuleClasspath && myModule.equals(((UsageInModuleClasspath)obj).myModule)
          && mySourceElement.equals(((UsageInModuleClasspath)obj).mySourceElement);
  }

  @Override
  public Icon getIcon() {
    return ModuleType.get(myModule).getNodeIcon(false);
  }
}
