/* eslint-disable @typescript-eslint/no-explicit-any */
export interface Response {
  body: any | null;
  statusCode: number;
  errorMessage: string | null;
}
