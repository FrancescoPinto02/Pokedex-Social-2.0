import type { FilterOption } from "./FilterOption";
import type { FilterRanges } from "./FilterRanges";

export interface Filters {
  types: FilterOption[];
  abilities: FilterOption[];
  ndexRange: FilterRanges;
  weightRange: FilterRanges;
  heightRange: FilterRanges;
}