package com.maxe10.gamulti;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashSet;

/**
 * Created by maxe10 on 09/05/2017.
 */

public class MultiDrawView extends View
{
  private static boolean mDrawMode = true;

  private static final int CIRCLE_SIZE = 75;
  private static final int MAX_DELTA_ALLOWED = 100;
  private static final int STEPS_SIZE_FOR_PATH_CHECKING = 1;

  private GestureDetector gestureDetector;

  private SparseArray<PointF> mActivePointers;
  private SparseArray<Path> mActivePaths;

  private HashSet<Path> mTemplatePaths;
  private HashSet<Path> templatePathsToCompare;
  private HashSet<Path> mCorrectPaths;
  private HashSet<Path> mWrongPaths;

  private Paint mPaint;
  private int[] colors = { Color.BLUE, Color.CYAN, Color.GRAY, Color.DKGRAY, Color.LTGRAY, Color.YELLOW, Color.MAGENTA };

  private Paint textPaint;

  public MultiDrawView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    initView();
  }

  private void initView()
  {
    mActivePointers = new SparseArray<PointF>();
    mActivePaths = new SparseArray<Path>();
    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    // set painter color to a color you like
    mPaint.setColor(Color.BLUE);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeWidth(6f);

    templatePathsToCompare = new HashSet<Path>();
    mCorrectPaths = new HashSet<Path>();
    mWrongPaths = new HashSet<Path>();


    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setTextSize(20);

    createTemplatePaths();

    for (Path tempPath : mTemplatePaths)
    {
      templatePathsToCompare.add(tempPath);
    }
  }

  private void createTemplatePaths()
  {
    mTemplatePaths = new HashSet<Path>();

    Path tempPath1 = new Path();
    tempPath1.moveTo(100, 100);
    tempPath1.lineTo(200, 1600);
    mTemplatePaths.add(tempPath1);

    Path tempPath2 = new Path();
    tempPath2.moveTo(800, 800);
    tempPath2.lineTo(800, 250);
    mTemplatePaths.add(tempPath2);
  }



  private void checkUserPath(Path userPath)
  {
    Path correspondingTemplatePath = null;

    for (Path templatePath : templatePathsToCompare)
    {
      if (comparePaths(userPath, templatePath))
      {
        correspondingTemplatePath = templatePath;
        break;
      }
    }

    if (correspondingTemplatePath != null)
    {
      System.out.println("Paths are nearly equals");
      mCorrectPaths.add(userPath);
      templatePathsToCompare.remove(correspondingTemplatePath);
    }
    else
    {
      System.out.println("No corresponding path found");
      mWrongPaths.add(userPath);
    }
  }

  @Nullable
  private Boolean comparePaths(Path userPath, Path templatePath)
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

  @NonNull
  private Boolean checkAllPathsPoints(PathMeasure templatePathMeasure, PathMeasure pathMeasure, boolean bSameDirection)
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

  private boolean checkPositionsCloseEnough(float[] userPosition, float[] templatePos)
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

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    if (!mDrawMode)
    {
      return false;
    }

    // get pointer index from the event object
    int pointerIndex = event.getActionIndex();

    // get pointer ID
    int pointerId = event.getPointerId(pointerIndex);

    // get masked (not specific to a pointer) action
    int maskedAction = event.getActionMasked();

    switch (maskedAction)
    {
      case MotionEvent.ACTION_DOWN:
      case MotionEvent.ACTION_POINTER_DOWN:
        // we have a new pointer. Lets add it to the list of pointers

        PointF f = new PointF();
        f.x = event.getX(pointerIndex);
        f.y = event.getY(pointerIndex);
        mActivePointers.put(pointerId, f);

        Path newPath = new Path();
        newPath.moveTo(f.x, f.y);
        mActivePaths.put(pointerId, newPath);
        break;

      case MotionEvent.ACTION_MOVE:
        for (int i = 0; i < event.getPointerCount(); i++)
        {
          PointF point = mActivePointers.get(event.getPointerId(i));
          if (point != null)
          {
            point.x = event.getX(i);
            point.y = event.getY(i);
          }
          Path path = mActivePaths.get(event.getPointerId(i));
          if (path != null)
          {
            path.lineTo(event.getX(i), event.getY(i));
          }
        }
        break;

      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
      case MotionEvent.ACTION_CANCEL:

        checkUserPath(mActivePaths.get(pointerId));
        mActivePointers.remove(pointerId);
        mActivePaths.remove(pointerId);

        if (mActivePointers.size() == 0)
        {
          mDrawMode = false;
        }
        break;
    }

    invalidate();
    return true;
  }

  @Override
  protected void onDraw(Canvas canvas)
  {
    super.onDraw(canvas);

    // draw all pointers
    for (int i = 0; i < mActivePointers.size(); i++)
    {
      mPaint.setColor(colors[i % 9]);

      PointF point = mActivePointers.valueAt(i);
      if (point != null)
      {
        canvas.drawCircle(point.x, point.y, CIRCLE_SIZE, mPaint);
      }

      Path path = mActivePaths.valueAt(i);
      if (path != null)
      {
        canvas.drawPath(path, mPaint);
      }
      canvas.drawText("Total pointers: " + mActivePointers.size(), 10, 40, textPaint);
    }

    // draw template paths
    mPaint.setColor(Color.BLACK);
    for (Path p : mTemplatePaths)
    {
      canvas.drawPath(p, mPaint);
    }

    // draw correct paths
    mPaint.setColor(Color.GREEN);
    for (Path p : mCorrectPaths)
    {
      canvas.drawPath(p, mPaint);
    }

    // draw wrong paths
    mPaint.setColor(Color.RED);
    for (Path p : mWrongPaths)
    {
      if (p != null)
      {
        canvas.drawPath(p, mPaint);
      }
    }
  }

  public void reset()
  {
    // reset the paths and pointers
    mActivePaths.clear();
    mActivePointers.clear();
    mCorrectPaths.clear();
    mWrongPaths.clear();

    // reset draw mode
    mDrawMode = true;

    // refill paths to compare for next round
    for (Path tempPath : mTemplatePaths)
    {
      templatePathsToCompare.add(tempPath);
    }

    invalidate();
  }
}
