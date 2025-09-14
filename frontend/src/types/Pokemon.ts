export interface Pokemon {
  id: number;
  ndex: number;
  species: string;
  forme: string;
  pokemonClass: string;
  types: { id: number; name: string }[];
  imageUrl: string;
}