export interface vestvalidatorPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  checkHasVest(options: HasVestOptions): Promise<{ hasVest: boolean }>;
}

export interface HasVestOptions {
  // Base64-encoded image data (e.g., data without the prefix), or a file URI
  image: string;
  showLogs?: boolean;
}

