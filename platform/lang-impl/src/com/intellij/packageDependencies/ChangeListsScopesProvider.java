/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package com.intellij.packageDependencies;

import com.intellij.ide.IdeBundle;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.changes.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.scope.packageSet.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author anna
 */
public class ChangeListsScopesProvider extends CustomScopesProviderEx {
  @NotNull
  private final Project myProject;

  public static ChangeListsScopesProvider getInstance(Project project) {
    return Extensions.findExtension(CUSTOM_SCOPES_PROVIDER, project, ChangeListsScopesProvider.class);
  }

  public ChangeListsScopesProvider(@NotNull Project project) {
    myProject = project;
  }

  @NotNull
  @Override
  public List<NamedScope> getCustomScopes() {

    if (myProject.isDefault() || !ProjectLevelVcsManager.getInstance(myProject).hasAnyMappings()) return Collections.emptyList();
    final ChangeListManager changeListManager = ChangeListManager.getInstance(myProject);

    final List<NamedScope> result = new ArrayList<>();
    result.add(createScope(changeListManager.getAffectedFiles(), IdeBundle.message("scope.modified.files")));
    for (ChangeList list : changeListManager.getChangeListsCopy()) {
      result.add(createChangeListScope(list));
    }
    return result;
  }

  private static NamedScope createChangeListScope(@NotNull ChangeList list) {
    final Collection<Change> changes = list.getChanges();
    return createScope(ChangesUtil.getAfterRevisionsFiles(changes.stream()).collect(Collectors.toList()), list.getName());
  }

  @Override
  public NamedScope getCustomScope(@NotNull String name) {
    if (myProject.isDefault()) return null;
    final ChangeListManager changeListManager = ChangeListManager.getInstance(myProject);
    if (IdeBundle.message("scope.modified.files").equals(name)) {
      return createScope(changeListManager.getAffectedFiles(), IdeBundle.message("scope.modified.files"));
    }
    final LocalChangeList changeList = changeListManager.findChangeList(name);
    if (changeList != null) {
      return createChangeListScope(changeList);
    }
    return null;
  }

  @Override
  public boolean isVetoed(NamedScope scope, ScopePlace place) {
    if (place == ScopePlace.SETTING) {
      if (myProject.isDefault()) return false;
      final ChangeListManager changeListManager = ChangeListManager.getInstance(myProject);
      return changeListManager.findChangeList(scope.getName()) != null;
    }
    return false;
  }

  @NotNull
  private static NamedScope createScope(@NotNull final List<VirtualFile> files, @NotNull String changeListName) {
    return new NamedScope(changeListName, new PackageSetBase() {
      @Override
      public boolean contains(VirtualFile file, NamedScopesHolder holder) {
        return files.contains(file);
      }

      @NotNull
      @Override
      public PackageSet createCopy() {
        return this;
      }

      @NotNull
      @Override
      public String getText() {
        return "file:*//*";
      }

      @Override
      public int getNodePriority() {
        return 0;
      }
    }){
      @Override
      public boolean equals(Object obj) {
        if (!(obj instanceof NamedScope)) return false;
        return getName().equals(((NamedScope)obj).getName());
      }

      @Override
      public int hashCode() {
        return getName().hashCode();
      }
    };
  }
}
