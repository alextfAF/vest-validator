export interface vestvalidatorPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
