package com.maxe10.gamulti;

import android.graphics.Path;
import android.graphics.PathMeasure;

import java.util.HashSet;

/**
 * Created by alme on 11.05.17.
 */

public class ScoreHandling
{
  private static final int MAX_DELTA_ALLOWED = 100;
  private static final int STEPS_SIZE_FOR_PATH_CHECKING = 1;

  public static boolean checkPositionsCloseEnough(float[] userPosition, float[] templatePos)
  {
    if (userPosition.length < 2 || templatePos.length < 2)
    {
      System.out.println("to few parameters for position check");
      return false;
    }

    float fDiffX = Math.abs(templatePos[0] - userPosition[0]);
    float fDiffY = Math.abs(templatePos[1] - userPosition[1]);
    double diffPos = Math.sqrt(fDiffX * fDiffX + fDiffY * fDiffY);

    return diffPos < MAX_DELTA_ALLOWED;
  }

  public static Boolean comparePaths(Path userPath, Path templatePath)
  {
    PathMeasure templatePathMeasure = new PathMeasure(templatePath, false);
    PathMeasure pathMeasure = new PathMeasure(userPath, false);

    System.out.println("templatePathMeasure.getLength(): " + templatePathMeasure.getLength());
    System.out.println("pathMeasure.getLength(): " + pathMeasure.getLength());
    System.out.println("Math.abs(templatePathMeasure.getLength() - pathMeasure.getLength()): " + Math.abs(templatePathMeasure.getLength() - pathMeasure.getLength()));

    if (Math.abs(templatePathMeasure.getLength() - pathMeasure.getLength()) > MAX_DELTA_ALLOWED)
    {
      System.out.println("NOT EQUALS: ONE PATH IS LONGER");
      return false;
    }

    float[] posTemplate = new float[2];
    float[] posPath = new float[2];
    float[] tanTemplate = new float[2];
    float[] tanPath = new float[2];

    boolean bSameDirectionEquals = false;
    boolean bOppositeDirectionEquals = false;

    // check if path could have same direction --> start points should be near
    templatePathMeasure.getPosTan(0.0f, posTemplate, tanTemplate);
    pathMeasure.getPosTan(0.0f, posPath, tanPath);
    if (checkPositionsCloseEnough(posPath, posTemplate))
    {
      bSameDirectionEquals = checkAllPathsPoints(templatePathMeasure, pathMeasure, true);
    }

    // check if path could have the opposite direction --> start and end point should be near
    templatePathMeasure.getPosTan(templatePathMeasure.getLength(), posTemplate, tanTemplate);
    pathMeasure.getPosTan(0.0f, posPath, tanPath);
    if (checkPositionsCloseEnough(posPath, posTemplate))
    {
      bOppositeDirectionEquals = checkAllPathsPoints(templatePathMeasure, pathMeasure, false);
    }

    return bSameDirectionEquals || bOppositeDirectionEquals;
  }
  
  private static Boolean checkAllPathsPoints(PathMeasure templatePathMeasure, PathMeasure pathMeasure, boolean bSameDirection)
  {
    float fSmallerLength = Math.min(templatePathMeasure.getLength(), pathMeasure.getLength());
    float fUserPathDistance = 0.0f;
    float fTemplatePathDistance = 0.0f;
    if (!bSameDirection)
    {
      fTemplatePathDistance = templatePathMeasure.getLength();
    }

    float[] posTemplate = new float[2];
    float[] posPath = new float[2];
    float[] tanTemplate = new float[2];
    float[] tanPath = new float[2];

    // check all points of the path
    while (fUserPathDistance <= fSmallerLength)
    {
      templatePathMeasure.getPosTan(fTemplatePathDistance, posTemplate, tanTemplate);
      pathMeasure.getPosTan(fUserPathDistance, posPath, tanPath);

      if (!checkPositionsCloseEnough(posPath, posTemplate))
      {
        System.out.println("NOT EQUALS: PATH ARE NOT CLOSE ENOUGH");
        return false;
      }
      fUserPathDistance += STEPS_SIZE_FOR_PATH_CHECKING;
      if (bSameDirection)
      {
        fTemplatePathDistance += STEPS_SIZE_FOR_PATH_CHECKING;
      }
      else
      {
        fTemplatePathDistance -= STEPS_SIZE_FOR_PATH_CHECKING;
      }
    }
    return true;
  }

  public static HashSet<Path> createTemplatePaths()
  {
    HashSet<Path> templatePaths = new HashSet<Path>();

    Path tempPath1 = new Path();
    tempPath1.moveTo(100, 100);
    tempPath1.lineTo(200, 1600);
    templatePaths.add(tempPath1);

    Path tempPath2 = new Path();
    tempPath2.moveTo(800, 800);
    tempPath2.lineTo(800, 250);
    templatePaths.add(tempPath2);

    return templatePaths;
  }

}
