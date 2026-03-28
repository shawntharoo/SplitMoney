import { Request } from 'express';

export type GraphqlViewer = {
  userId?: string;
};

export type GraphqlContext = {
  req: Request;
  viewer: GraphqlViewer;
};
