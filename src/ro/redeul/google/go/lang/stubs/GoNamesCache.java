package ro.redeul.google.go.lang.stubs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.containers.HashSet;
import ro.redeul.google.go.lang.psi.GoFile;
import ro.redeul.google.go.lang.psi.stubs.index.GoPackageImportPath;
import ro.redeul.google.go.lang.psi.stubs.index.GoPackageName;
import ro.redeul.google.go.lang.psi.stubs.index.GoTypeName;
import ro.redeul.google.go.lang.psi.toplevel.GoTypeNameDeclaration;

/**
 * Author: Toader Mihai Claudiu <mtoader@gmail.com>
 * <p/>
 * Date: 5/19/11
 * Time: 8:04 PM
 */
public class GoNamesCache {

    private final Project myProject;

    // TODO: Make this a singleton ?!
    @NotNull
    public static GoNamesCache getInstance(Project project) {
        return ServiceManager.getService(project, GoNamesCache.class);
	}

    public GoNamesCache(Project project) {
        this.myProject = project;
    }

    public Collection<String> getProjectPackages() {
        return getPackagesInScope(GlobalSearchScope.projectScope(myProject));
    }

    public Collection<String> getSdkPackages() {
        return getPackagesInScope(GlobalSearchScope.notScope(
            GlobalSearchScope.projectScope(myProject)));
    }

    public Collection<String> getAllPackages() {
        return getPackagesInScope(GlobalSearchScope.allScope(myProject));
    }

    public Collection<String> getPackagesInScope(GlobalSearchScope scope) {

        StubIndex index = StubIndex.getInstance();

        Collection<String> keys = index.getAllKeys(GoPackageImportPath.KEY, myProject);

        Collection<String> packagesCollection = new ArrayList<String>();

        for (String key : keys) {
            Collection<GoFile> files = index.get(GoPackageImportPath.KEY, key, myProject, scope);
            if (files != null && files.size() > 0) {
                packagesCollection.add(key);
            }
        }

        return packagesCollection;
    }

    public Collection<GoFile> getBuiltinPackageFiles() {
        return getFilesByPackageName("builtin");
    }

    public Collection<GoFile> getFilesByPackageName(String packageName) {
        StubIndex index = StubIndex.getInstance();

        return index.get(GoPackageName.KEY, packageName, myProject,
                         GlobalSearchScope.allScope(myProject));
    }

    public Collection<GoFile> getFilesByPackageImportPath(@NotNull String importPath) {
        return getFilesByPackageImportPath(importPath, GlobalSearchScope.allScope(myProject));
    }

    public Collection<GoFile> getFilesByPackageImportPath(@NotNull String importPath,
                                                          @NotNull GlobalSearchScope scope) {
        StubIndex index = StubIndex.getInstance();

        return index.get(GoPackageImportPath.KEY, importPath, myProject, scope);
    }


    private GlobalSearchScope getSearchScope(boolean allScope) {
        return
            allScope
                ? GlobalSearchScope.allScope(myProject)
                : GlobalSearchScope.projectScope(myProject);
    }

    @NotNull
    public NavigationItem[] getTypesByName(@NotNull @NonNls String name,
                                           boolean includeNonProjectItems) {
        StubIndex index = StubIndex.getInstance();
        GlobalSearchScope scope = getSearchScope(includeNonProjectItems);
        Collection<NavigationItem> items = new ArrayList<NavigationItem>();
        for (GoTypeNameDeclaration type : index.get(GoTypeName.KEY, name, myProject, scope)) {
            if (type instanceof NavigationItem) {
                items.add((NavigationItem) type);
            }
        }

        return items.toArray(new NavigationItem[items.size()]);
    }

    @NotNull
    public String[] getAllTypeNames() {
        HashSet<String> classNames = new HashSet<String>();
        getAllTypeNames(classNames);
        return classNames.toArray(new String[classNames.size()]);
    }

    public void getAllTypeNames(@NotNull Set<String> dest) {
        StubIndex index = StubIndex.getInstance();
        dest.addAll(index.getAllKeys(GoTypeName.KEY, myProject));
    }

    @NotNull
    public NavigationItem[] getFunctionsByName(@NotNull @NonNls String name,
                                               boolean includeNonProjectItems) {
        return new NavigationItem[0];
    }

    @NotNull
    public String[] getAllFunctionNames() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public void getAllFunctionNames(@NotNull Set<String> set) {
    }

    @NotNull
    public NavigationItem[] getVariablesByName(@NotNull @NonNls String name,
                                               boolean includeNonProjectItems) {
        return new NavigationItem[0];
    }

    @NotNull
    public String[] getAllVariableNames() {
        return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public void getAllVariableNames(@NotNull Set<String> set) {
    }
}
