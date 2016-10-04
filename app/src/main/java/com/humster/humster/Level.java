package com.humster.humster;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

/** Draws and progresses the current level */
class Level {
    private static final float TRACK_DEFAULT_WIDTH = 2;
    private int mLevelNumber;
    private boolean mLevelBuilt = false;
    private ArrayList<TrackPiece> mTrackPieces = new ArrayList<>();
    private TrackPiece mCurrentTrackPiece;

    private float mEndDistance;

    public Level(Context context, int levelNumber) {
        Log.i(HumsterGameView.LOG_TAG, "Setting up level " + levelNumber);
        mLevelNumber = levelNumber;
        mEndDistance = 0;
        mLevelBuilt = buildTrackFromJsonResource(context, R.raw.levels);
    }

    public boolean levelIsBuilt() {
        return mLevelBuilt;
    }

    public float getEndDistance() {
        return mEndDistance;
    }

    class TrackPiece implements IDrawable {
        private final int id;
        private float mLength, mStartWidth, mEndWidth, mStartDistance, mPosition;
        private ArrayList<TrackJoin> mTrackJoins = new
                ArrayList<TrackJoin>();
        private int trackType;

        public static final int TRACK_TYPE_DEFAULT = 1;

        public TrackPiece(int pieceID, float length, float position) {
            id = pieceID;
            mLength = length;
            mPosition = position;
            this.setWidth(TRACK_DEFAULT_WIDTH); // default
        }

        class TrackJoin {
            private int mTrackPieceID;
            private float mOffset; // 0 by default
            private int mJoinType;
            public TrackPiece joinsTo;

            public static final int JOIN_TYPE_DEFAULT = 1;
            public static final int JOIN_TYPE_CURVETO = 2;

            public TrackJoin(int trackPieceID, int joinType, float
                    offset) {
                mTrackPieceID = trackPieceID;
                mJoinType = joinType;
                mOffset = offset;
            }

            public int getTrackPieceID() {
                return mTrackPieceID;
            }

            public float getOffset() {
                return mOffset;
            }

            public int getJoinType() {
                return mJoinType;
            }
        }

        public void setWidth(float width) {
            mStartWidth = width;
            mEndWidth = width;
        }

        public void setWidth(float startWidth, float endWidth) {
            mStartWidth = startWidth;
            mEndWidth = endWidth;
        }

        public float getStartWidth() {
            return mStartWidth;
        }

        public float getEndWidth() {
            return mEndWidth;
        }

        public float getPosition() {
            return mPosition;
        }

        public float getLength() {
            return mLength;
        }

        public ArrayList<TrackJoin> getTrackJoins() {
            return mTrackJoins;
        }

        public void setStartDistance(float startDistance) {
            mStartDistance = startDistance;
        }

        public float endDistance() {
            return mStartDistance + mLength;
        }

        public float length() {
            return mLength;
        }

        public float startDistance() {
            return mStartDistance;
        }

        public void addJoin(int trackPieceID, int joinType, float offset) {
            mTrackJoins.add(new TrackJoin(trackPieceID, joinType, offset));
        }

        public ArrayList<TrackJoin> joins() {
            return mTrackJoins;
        }
    }

    public ArrayList<TrackPiece> getTrackPieces() {
        return mTrackPieces;
    }

    private boolean buildTrackFromJsonResource(Context context, int resource) {
        InputStream f = context.getResources().openRawResource(resource);
        String jsonString = convertStreamToString(f);
        try {
            JSONObject parent = new JSONObject(jsonString);
            JSONArray levelsJSON = parent.getJSONArray("levels");
            JSONObject levelJSON = getJsonObjectByID(levelsJSON, mLevelNumber);
            JSONArray trackPiecesJSON = levelJSON.getJSONArray("trackPieces");
            getTrackPiecesFromJson(trackPiecesJSON);
            arrangeTrackPieces();
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private void getTrackPiecesFromJson(JSONArray jsonPieces) throws JSONException {
        for (int i = 0; i < jsonPieces.length(); i++) {
            TrackPiece trackPiece = getTrackPieceFromJson(jsonPieces.getJSONObject(i));
            mTrackPieces.add(trackPiece);
        }
    }

    private TrackPiece getTrackPieceFromJson(JSONObject trackPiece) throws JSONException {
        int id = trackPiece.getInt("id");
        float position = (float) trackPiece.getDouble("position");
        float length = (float) trackPiece.getDouble("length");
        TrackPiece piece = new TrackPiece(id, length, position);
        if (trackPiece.has("startWidth")) {
            float startWidth = (float) trackPiece.getDouble("startWidth");
            if (trackPiece.has("endWidth")) {
                float endWidth = (float) trackPiece.getDouble("endWidth");
                piece.setWidth(startWidth, endWidth);
            }
        } else {
            if (trackPiece.has("width")) {
                float width = (float) trackPiece.getDouble("width");
                piece.setWidth(width);
            }
        }
        if (trackPiece.has("joinsTo")) addJoinsFromJson(trackPiece, piece);
        return piece;
    }

    private void addJoinsFromJson(JSONObject trackPiece, TrackPiece piece) throws JSONException {
        JSONArray joinArray = trackPiece.getJSONArray("joinsTo");
        for (int i = 0; i < joinArray.length(); i++) {
            JSONObject join = joinArray.getJSONObject(i);
            int trackPieceID = join.getInt("trackPieceID");
            float offset = (float) getJSONDouble(join, "offset", 0);
            int joinType = getJSONInt(join, "joinType",
                    TrackPiece.TrackJoin.JOIN_TYPE_DEFAULT);
            piece.addJoin(trackPieceID, joinType, offset);
        }
    }

    private JSONObject getJsonObjectByID(JSONArray jsonArray, int id) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            if (obj.getInt("id") == id) return obj;
        }
        return null;
    }

    private double getJSONDouble(JSONObject obj, String key, double defaultVal) {
        try {
            return obj.getDouble(key);
        } catch (JSONException e) {
            return defaultVal;
        }
    }

    private int getJSONInt(JSONObject obj, String key, int defaultVal) {
        return (int) getJSONDouble(obj, key, defaultVal);
    }

    /** Due to http://stackoverflow.com/a/5445161/2071807 */
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void arrangeTrackPieces() {
        TrackPiece firstPiece = mTrackPieces.get(0);
        mCurrentTrackPiece = firstPiece;
        arrangeTrackPiece(firstPiece, 0);
    }

    /** Recursively set the startDistance for each trackPiece following
     * a chain of joinsTo arrays
     */
    private void arrangeTrackPiece(TrackPiece trackPiece, float startDistance) {
        Log.i(HumsterGameView.LOG_TAG, "Track piece " + trackPiece.id + " starts at " + startDistance +
            " with length " + trackPiece.length());
        trackPiece.setStartDistance(startDistance);
        ArrayList<TrackPiece.TrackJoin> joins = trackPiece.joins();
        float endDistance = startDistance + trackPiece.getLength();
        for (int i = 0; i < joins.size(); i++) {
            TrackPiece.TrackJoin join = joins.get(i);
            int joinsToID = join.getTrackPieceID();
            TrackPiece joinsTo = mTrackPieces.get(joinsToID - 1);
            join.joinsTo = joinsTo;
            float offset = join.getOffset();
            float length = trackPiece.length();
            float nextStartDistance = startDistance + length + offset;
            arrangeTrackPiece(joinsTo, nextStartDistance);
        }
        if (endDistance > mEndDistance) mEndDistance = endDistance;
    }

}
