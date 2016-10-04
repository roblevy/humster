package com.humster.humster;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import java.util.HashMap;

import static android.graphics.Path.Direction.CW;

/**
 * Created by rob on 19/09/16.
 * This class knows how to draw the various elements of the game
 */
public class Renderer {
    private static float PIXELS_PER_DISTANCE = 3f; // Scale
    private static float NOTES_ON_SCREEN = 24f; // Number of divisions for vertical scale
    private static float CHARACTER_RADIUS = 100f;
    private static float POINTER_SIZE = 80f;
    private float mDistance = 0; // Distance of the left-hand edge of the screen
    private Paint mCharacterPaint, mTrackPaint, mPointerPaint;
    private static float[] SAFE_HSV = {56, 100, 100};
    private static float[] DANGER_HSV = {0, 100, 100};
    private HashMap<TrackPiecePair, Path> mTrackJoinPaths = new HashMap<>();
    protected int height, width;
    private float mFractionCharacterOnTrack = 1;
    Bitmap mBackground;
    Path mTrackPath, mPointerPath;

    Renderer(Bitmap background) {
        // Create Paint objects which set colour etc.
        mCharacterPaint = new Paint();
        mCharacterPaint.setAntiAlias(true);
        mCharacterPaint.setColor(Color.HSVToColor(SAFE_HSV));
        mCharacterPaint.setARGB(230, 255, 240, 20); // A random colour

        mTrackPaint = new Paint();
        mTrackPaint.setAntiAlias(true);
        mTrackPaint.setStyle(Paint.Style.FILL);
        mTrackPaint.setARGB(255, 190, 190, 190); // Light grey

        mPointerPaint = new Paint();
        mPointerPaint.setAntiAlias(true);
        mPointerPaint.setARGB(200, 100, 100, 180);

        mBackground = background;
        mTrackPath = new Path();
        mPointerPath = pointerPath();
    }

    private class TrackPiecePair {
        public Level.TrackPiece t1, t2;
        public TrackPiecePair(Level.TrackPiece t1, Level.TrackPiece t2) {
            this.t1 = t1;
            this.t2 = t2;
        }
    }

    void setCharacterColour(float safetyLevel) {
        float[] c = new float[3];
        float[] from = SAFE_HSV;
        float[] to = DANGER_HSV;

        for (int i = 0; i < c.length; i++) {
            c[i] = ((to[i] - from[i]) * safetyLevel) + from[i];
        }
        mCharacterPaint.setColor(Color.HSVToColor(c));
    }

    private int distanceToPixel(float distance) {
        return (int) (distance * PIXELS_PER_DISTANCE);
    }

    private int horizontalDistanceToPixel(float distance) {
        float relativeDistance = distance - mDistance;
        return distanceToPixel(relativeDistance);
    }

    private double visibleDistanceUnits(Canvas c) {
        return width / PIXELS_PER_DISTANCE;
    }

    private double lastVisibleDistanceUnit(Canvas c) {
        return mDistance + visibleDistanceUnits(c);
    }

    private boolean pointIsVisible(Canvas c, float distance) {
        return (distance > mDistance && distance < lastVisibleDistanceUnit(c));
    }

    /** TODO: This needs thinking about: an object is still visible if it
     * totally fills the screen!
     */
    private boolean isVisible(Canvas c, float startDistance, float endDistance) {
        // return (pointIsVisible(c, startDistance) || pointIsVisible(c, endDistance));
        return true;
    }

    /** Set the distance associated with the left-hand edge of the screen */
    void setDistance(float distance) {
        mDistance = distance;
    }

    void renderBackground(Canvas c) {
        int bgWidth = mBackground.getWidth();
        int cWidth = c.getWidth();
        int remainder = (int) (mDistance % bgWidth);
        c.drawBitmap(mBackground, -remainder, 0f, null);
        c.drawBitmap(mBackground, bgWidth - remainder, 0f, null);
    }

    void renderPointer(Canvas c, float y) {
        Path pointer = offsetPath(mPointerPath, 0, y);
        c.drawPath(pointer, mPointerPaint);
    }

    void renderCharacter(Canvas c, Path characterPath) {
        c.drawPath(characterPath, mCharacterPaint);
    }

    private Path characterPath(float x, float y, float z) {
        Path character = new Path();
        float radius = CHARACTER_RADIUS * (1 + z / 20);
        character.addCircle(x, y, radius, CW);
        return character;
    }

    private Path pointerPath() {
        Path pointer = new Path();
        float r = POINTER_SIZE;
        pointer.lineTo(0, r / 2f);
        // Pythagoras: point of equilateral triangle is at sqrt(r^2 - (r/2)^2)
        pointer.lineTo((float) Math.sqrt(3f / 4f * r * r), 0);
        pointer.lineTo(0, -r / 2f);
        pointer.close();
        return pointer;
    }

    private float fractionOfCharacterOnTrack(Path characterPath, Path trackPath) {
        RectF characterBounds = new RectF(); // i.e. float Rectangle
        RectF intersectBounds = new RectF();
        Path intersectPath = new Path();

        intersectPath.op(characterPath, trackPath, Path.Op.INTERSECT);

        characterPath.computeBounds(characterBounds, false);
        intersectPath.computeBounds(intersectBounds, false);
        float intersectArea = intersectBounds.height() * intersectBounds.width();
        float characterArea = characterBounds.height() * characterBounds.width();
        return Math.abs(intersectArea / characterArea);
    }

    float getFractionOfCharacterOnTrack() {
        return mFractionCharacterOnTrack;
    }

    /** Here's where the hard work of laying out the level happens */
    void createLevel(Level level) {
        // Render track
        for (Level.TrackPiece t: level.getTrackPieces()) {
                renderTrackPiece(t);
                renderTrackJoin(t);
        }
    }

    /** Draw the track in the correct location and render the character on top */
    void renderLevel(Canvas c, float characterX, float characterY, float characterZ) {
        Path transformedTrackPath = levelPathTransform(mTrackPath);
        Path characterPath = characterPath(characterX, characterY, characterZ);
        c.drawPath(transformedTrackPath, mTrackPaint);
        renderCharacter(c, characterPath);
        mFractionCharacterOnTrack = fractionOfCharacterOnTrack(characterPath, transformedTrackPath);
    }

    Path levelPathTransform(Path levelPath) {
        Path levelScaled = verticalScalePath(levelPath, height);
        Path levelOffset = offsetPath(levelScaled, -distanceToPixel(mDistance), height / 2f);
        return levelOffset;
    }

    private void renderTrackPiece(Level.TrackPiece t) {
        float startWidth = verticalScale(t.getStartWidth());
        float endWidth = verticalScale(t.getEndWidth());
        float position = verticalScale(t.getPosition());
        float start = horizontalDistanceToPixel(t.startDistance());
        float end = horizontalDistanceToPixel(t.endDistance());
        float startTop = getTop(position, startWidth);
        float startBottom = getBottom(position, startWidth);
        float endTop = getTop(position, endWidth);
        float endBottom = getBottom(position, endWidth);

        Path p = new Path();
        p.moveTo(start, startTop);
        p.lineTo(end, endTop);
        p.lineTo(end, endBottom);
        p.lineTo(start, startBottom);
        p.close();
        mTrackPath.addPath(p);
    }

    private float getTop(float centre, float width) {
        return centre - width / 2;
    }

    private float getBottom(float centre, float width) {
        return centre + width / 2;
    }

    private float verticalScale(float position) {
        return position / (NOTES_ON_SCREEN / 2f);
    }

    private boolean wideCurveFirst(float startX, float startY, float endX, float endY) {
        return (startY < endY && startX < endX) || (startY > endY && startX > endX);
    }

    /** Draw a single Bezier cubic which makes up one half of a track turn */
    private void addHalfCurveTo(Path p, float startX, float startY, float endX, float endY, boolean flip) {
        float xDiff, yDiff;
        float x1, x2, x3, y1, y2, y3;
        xDiff = endX - startX;
        yDiff = endY - startY;
        if (flip) {
            x1 = startX;
            y1 = startY + yDiff / 2f;
            x2 = startX + xDiff / 2f;
            y2 = endY;
        } else {
            x1 = startX + xDiff / 2f;
            y1 = startY;
            x2 = endX;
            y2 = startY + yDiff / 2f;
        }
        x3 = endX;
        y3 = endY;
        p.cubicTo(x1, y1, x2, y2, x3, y3);
    }

    /** Draw an S-shaped curve made of two Bezier cubics.
     */
    private void addCurveTo(Path p, float startX, float startY, float endX, float endY) {
        // This curve needs splitting into two parts to
        // ensure the track stays parallel. See this:
        //http://math.stackexchange.com/questions/465782/control-points-of-offset-bezier-curve
        // The curve has a narrow (inside) edge and a broad (outside) edge.
        // The order in which these are needed depends on which direction we're going in.
        boolean wideFirst = wideCurveFirst(startX, startY, endX, endY);
        float xDiff, yDiff, midX, midY, midPointFraction;
        xDiff = endX - startX;
        yDiff = endY - startY;
        if (wideFirst) {
            midPointFraction = 2 / 3f;
        } else {
            midPointFraction = 1 / 3f;
        }
        midX = startX + midPointFraction * xDiff;
        midY = startY + midPointFraction * yDiff;
        addHalfCurveTo(p, startX, startY, midX, midY, false);
        addHalfCurveTo(p, midX, midY, endX, endY, true);
    }

    private class xy {
        float x, y;
        public xy(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    private Path curvedJoin(xy startTop, xy startBottom, xy endTop, xy endBottom) {
        Path p = new Path();
        p.moveTo(startTop.x, startTop.y);
        addCurveTo(p, startTop.x, startTop.y, endTop.x, endTop.y);
        p.lineTo(endBottom.x, endBottom.y);
        addCurveTo(p, endBottom.x, endBottom.y, startBottom.x, startBottom.y);
        p.close();
        return p;
    }

    private void renderTrackJoin(Level.TrackPiece t1) {
        for (Level.TrackPiece.TrackJoin j: t1.getTrackJoins()) {
            Level.TrackPiece t2 = j.joinsTo;
            TrackPiecePair joinPieces = new TrackPiecePair(t1, t2);
            float xOffset = distanceToPixel(t1.endDistance() - mDistance);
            switch(j.getJoinType()) {
                case Level.TrackPiece.TrackJoin.JOIN_TYPE_DEFAULT:
                    // Do nothing
                    break;
                case Level.TrackPiece.TrackJoin.JOIN_TYPE_CURVETO:
                    if (!mTrackJoinPaths.containsKey(joinPieces)) {
                        mTrackJoinPaths.put(joinPieces, curvedJoinPath(t1, t2));
                    }
                    renderPath(mTrackJoinPaths.get(joinPieces), xOffset);
                    break;
            }
        }
    }

    private void renderPath(Path p, float xOffset) {
        mTrackPath.addPath(offsetPath(p, xOffset, 0));
    }

    private Path offsetPath(Path p, float xOffset, float yOffset) {
        Path pTransformed = new Path(p);
        Matrix m = new Matrix();
        m.setTranslate(xOffset, yOffset);
        pTransformed.transform(m);
        return pTransformed;
    }

    private Path verticalScalePath(Path p, float verticalScale) {
        Path pTransformed = new Path(p);
        Matrix m = new Matrix();
        m.setScale(1, verticalScale);
        pTransformed.transform(m);
        return pTransformed;
    }

    private Path curvedJoinPath(Level.TrackPiece t1, Level.TrackPiece t2) {
        float joinLength = t2.startDistance() - t1.endDistance();
        float endX = distanceToPixel(joinLength);
        float startWidth = verticalScale(t1.getEndWidth());
        float endWidth = verticalScale(t2.getStartWidth());
        float position1 = verticalScale(t1.getPosition());
        float position2 = verticalScale(t2.getPosition());
        float startTopY = getTop(position1, startWidth);
        float startBottomY = getBottom(position1, startWidth);
        float endTopY = getTop(position2, endWidth);
        float endBottomY = getBottom(position2, endWidth);
        xy startTop = new xy(0, startTopY);
        xy startBottom = new xy(0, startBottomY);
        xy endTop = new xy(endX, endTopY);
        xy endBottom = new xy(endX, endBottomY);
        return curvedJoin(startTop, startBottom, endTop, endBottom);
    }
}
