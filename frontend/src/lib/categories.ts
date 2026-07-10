import type { CategoryTree } from "./api";

export function formatCategoryLabel(
  category: Pick<CategoryTree, "name" | "parentChain">
): string {
  const depth = category.parentChain.length;
  if (depth === 0) return category.name;
  return `${"— ".repeat(depth)}${category.name}`;
}

export function formatParentChain(
  category: Pick<CategoryTree, "parentChain">
): string {
  if (category.parentChain.length === 0) return "Root category";
  return category.parentChain.map((parent) => parent.name).join(" › ");
}

export function sortCategoriesByTreeOrder<T extends Pick<CategoryTree, "name" | "parentChain">>(
  categories: T[]
): T[] {
  return [...categories].sort((a, b) => {
    const depthDiff = a.parentChain.length - b.parentChain.length;
    if (depthDiff !== 0) return depthDiff;
    return a.name.localeCompare(b.name);
  });
}
