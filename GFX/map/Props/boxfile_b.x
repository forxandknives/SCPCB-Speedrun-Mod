xof 0303txt 0032
template XSkinMeshHeader {
 <3cf169ce-ff7c-44ab-93c0-f78f62d172e2>
 WORD nMaxSkinWeightsPerVertex;
 WORD nMaxSkinWeightsPerFace;
 WORD nBones;
}

template VertexDuplicationIndices {
 <b8d65549-d7c9-4995-89cf-53a9a8b031e3>
 DWORD nIndices;
 DWORD nOriginalVertices;
 array DWORD indices[nIndices];
}

template SkinWeights {
 <6f0d123b-bad2-4167-a0d0-80224f25fabb>
 STRING transformNodeName;
 DWORD nWeights;
 array DWORD vertexIndices[nWeights];
 array FLOAT weights[nWeights];
 Matrix4x4 matrixOffset;
}

template AnimTicksPerSecond {
 <9e415a43-7ba6-4a73-8743-b73d47e88476>
 DWORD AnimTicksPerSecond;
}

template FVFData {
 <b6e70a0e-8ef9-4e83-94ad-ecc8b0c04897>
 DWORD dwFVF;
 DWORD nDWords;
 array DWORD data[nDWords];
}


AnimTicksPerSecond {
 24;
}

Frame Body {
 

 FrameTransformMatrix {
  1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000;;
 }

 Mesh Body {
  24;
  2.224800;18.319014;-7.052299;,
  2.224800;18.319014;7.052303;,
  -2.224800;18.319014;7.052303;,
  -2.224800;18.319014;-7.052299;,
  2.224800;0.000000;-7.052299;,
  2.224800;0.000000;7.052204;,
  2.224800;18.319014;7.052303;,
  2.224800;18.319014;-7.052299;,
  -2.224800;0.000000;-7.052299;,
  -2.224800;0.000000;7.052204;,
  2.224800;0.000000;7.052204;,
  2.224800;0.000000;-7.052299;,
  -2.224800;18.319014;-7.052299;,
  -2.224800;18.319014;7.052303;,
  -2.224800;0.000000;7.052204;,
  -2.224800;0.000000;-7.052299;,
  2.224800;18.319014;7.052303;,
  2.224800;0.000000;7.052204;,
  -2.224800;0.000000;7.052204;,
  -2.224800;18.319014;7.052303;,
  -2.224800;18.319014;-7.052299;,
  -2.224800;0.000000;-7.052299;,
  2.224800;0.000000;-7.052299;,
  2.224800;18.319014;-7.052299;;
  12;
  3;0,2,1;,
  3;0,3,2;,
  3;4,6,5;,
  3;4,7,6;,
  3;8,10,9;,
  3;8,11,10;,
  3;12,14,13;,
  3;12,15,14;,
  3;16,18,17;,
  3;16,19,18;,
  3;20,22,21;,
  3;20,23,22;;

  MeshNormals {
   24;
   0.577350;0.577350;-0.577350;,
   0.577351;0.577348;0.577351;,
   -0.577351;0.577348;0.577351;,
   -0.577350;0.577350;-0.577350;,
   0.577350;-0.577350;-0.577350;,
   0.577349;-0.577352;0.577349;,
   0.577351;0.577348;0.577351;,
   0.577350;0.577350;-0.577350;,
   -0.577350;-0.577350;-0.577350;,
   -0.577349;-0.577352;0.577349;,
   0.577349;-0.577352;0.577349;,
   0.577350;-0.577350;-0.577350;,
   -0.577350;0.577350;-0.577350;,
   -0.577351;0.577348;0.577351;,
   -0.577349;-0.577352;0.577349;,
   -0.577350;-0.577350;-0.577350;,
   0.577351;0.577348;0.577351;,
   0.577349;-0.577352;0.577349;,
   -0.577349;-0.577352;0.577349;,
   -0.577351;0.577348;0.577351;,
   -0.577350;0.577350;-0.577350;,
   -0.577350;-0.577350;-0.577350;,
   0.577350;-0.577350;-0.577350;,
   0.577350;0.577350;-0.577350;;
   12;
   3;0,2,1;,
   3;0,3,2;,
   3;4,6,5;,
   3;4,7,6;,
   3;8,10,9;,
   3;8,11,10;,
   3;12,14,13;,
   3;12,15,14;,
   3;16,18,17;,
   3;16,19,18;,
   3;20,22,21;,
   3;20,23,22;;
  }

  MeshTextureCoords {
   24;
   0.659723;0.848511;,
   0.659723;0.560997;,
   0.578358;0.560997;,
   0.578358;0.848511;,
   0.543121;0.921943;,
   0.801038;0.921943;,
   0.801040;0.548520;,
   0.543121;0.548520;,
   0.666123;0.560997;,
   0.666123;0.848509;,
   0.747489;0.848509;,
   0.747489;0.560997;,
   0.829468;0.544725;,
   0.571550;0.544725;,
   0.571551;0.918148;,
   0.829468;0.918148;,
   0.807440;0.548520;,
   0.807440;0.921943;,
   0.888806;0.921943;,
   0.888806;0.548520;,
   0.306197;0.065304;,
   0.306197;0.650903;,
   0.382298;0.650903;,
   0.382298;0.065304;;
  }

  VertexDuplicationIndices {
   24;
   24;
   0,
   1,
   2,
   3,
   4,
   5,
   6,
   7,
   8,
   9,
   10,
   11,
   12,
   13,
   14,
   15,
   16,
   17,
   18,
   19,
   20,
   21,
   22,
   23;
  }

  MeshMaterialList {
   1;
   12;
   0,
   0,
   0,
   0,
   0,
   0,
   0,
   0,
   0,
   0,
   0,
   0;

   Material def_surf_mat {
    0.992157;0.992157;0.992157;1.000000;;
    128.000000;
    0.149020;0.149020;0.149020;;
    0.000000;0.000000;0.000000;;

    TextureFilename {
     "boxfile_a.jpg";
    }
   }
  }

  XSkinMeshHeader {
   1;
   1;
   1;
  }

  SkinWeights {
   "Body";
   24;
   0,
   1,
   2,
   3,
   4,
   5,
   6,
   7,
   8,
   9,
   10,
   11,
   12,
   13,
   14,
   15,
   16,
   17,
   18,
   19,
   20,
   21,
   22,
   23;
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000,
   1.000000;
   1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000,0.000000,0.000000,0.000000,0.000000,1.000000;;
  }
 }
}