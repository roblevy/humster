package com.humster.humster;

import android.util.Log;
import java.util.ArrayList;

class Notes {
    class Note {
        int id; String name, sharp_name, flat_name; float freq;
    }

    public ArrayList<Note> notes = new ArrayList<Note>();
    public static final float MIN_FREQ=65.41f;
    public static final float MAX_FREQ=880f;
    public static final int MAX_NOTE_ID=46;

    public Notes() {
        Note note;
        // Note C2
        note = new Note();
        note.id=1;note.name="C2";note.sharp_name="C2";note.flat_name="C2";note.freq=65.41f;
        notes.add(note);
        // Note C#2/Db2
        note = new Note();
        note.id=2;note.name="C#2/Db2";note.sharp_name="C#2";note.flat_name="Db2";note.freq=69.3f;
        notes.add(note);
        // Note D2
        note = new Note();
        note.id=3;note.name="D2";note.sharp_name="D2";note.flat_name="D2";note.freq=73.42f;
        notes.add(note);
        // Note D#2/Eb2
        note = new Note();
        note.id=4;note.name="D#2/Eb2";note.sharp_name="D#2";note.flat_name="Eb2";note.freq=77.78f;
        notes.add(note);
        // Note E2
        note = new Note();
        note.id=5;note.name="E2";note.sharp_name="E2";note.flat_name="E2";note.freq=82.41f;
        notes.add(note);
        // Note F2
        note = new Note();
        note.id=6;note.name="F2";note.sharp_name="F2";note.flat_name="F2";note.freq=87.31f;
        notes.add(note);
        // Note F#2/Gb2
        note = new Note();
        note.id=7;note.name="F#2/Gb2";note.sharp_name="F#2";note.flat_name="Gb2";note.freq=92.5f;
        notes.add(note);
        // Note G2
        note = new Note();
        note.id=8;note.name="G2";note.sharp_name="G2";note.flat_name="G2";note.freq=98f;
        notes.add(note);
        // Note G#2/Ab2
        note = new Note();
        note.id=9;note.name="G#2/Ab2";note.sharp_name="G#2";note.flat_name="Ab2";note.freq=103.83f;
        notes.add(note);
        // Note A2
        note = new Note();
        note.id=10;note.name="A2";note.sharp_name="A2";note.flat_name="A2";note.freq=110f;
        notes.add(note);
        // Note A#2/Bb2
        note = new Note();
        note.id=11;note.name="A#2/Bb2";note.sharp_name="A#2";note.flat_name="Bb2";note.freq=116.54f;
        notes.add(note);
        // Note B2
        note = new Note();
        note.id=12;note.name="B2";note.sharp_name="B2";note.flat_name="B2";note.freq=123.47f;
        notes.add(note);
        // Note C3
        note = new Note();
        note.id=13;note.name="C3";note.sharp_name="C3";note.flat_name="C3";note.freq=130.81f;
        notes.add(note);
        // Note C#3/Db3
        note = new Note();
        note.id=14;note.name="C#3/Db3";note.sharp_name="C#3";note.flat_name="Db3";note.freq=138.59f;
        notes.add(note);
        // Note D3
        note = new Note();
        note.id=15;note.name="D3";note.sharp_name="D3";note.flat_name="D3";note.freq=146.83f;
        notes.add(note);
        // Note D#3/Eb3
        note = new Note();
        note.id=16;note.name="D#3/Eb3";note.sharp_name="D#3";note.flat_name="Eb3";note.freq=155.56f;
        notes.add(note);
        // Note E3
        note = new Note();
        note.id=17;note.name="E3";note.sharp_name="E3";note.flat_name="E3";note.freq=164.81f;
        notes.add(note);
        // Note F3
        note = new Note();
        note.id=18;note.name="F3";note.sharp_name="F3";note.flat_name="F3";note.freq=174.61f;
        notes.add(note);
        // Note F#3/Gb3
        note = new Note();
        note.id=19;note.name="F#3/Gb3";note.sharp_name="F#3";note.flat_name="Gb3";note.freq=185f;
        notes.add(note);
        // Note G3
        note = new Note();
        note.id=20;note.name="G3";note.sharp_name="G3";note.flat_name="G3";note.freq=196f;
        notes.add(note);
        // Note G#3/Ab3
        note = new Note();
        note.id=21;note.name="G#3/Ab3";note.sharp_name="G#3";note.flat_name="Ab3";note.freq=207.65f;
        notes.add(note);
        // Note A3
        note = new Note();
        note.id=22;note.name="A3";note.sharp_name="A3";note.flat_name="A3";note.freq=220f;
        notes.add(note);
        // Note A#3/Bb3
        note = new Note();
        note.id=23;note.name="A#3/Bb3";note.sharp_name="A#3";note.flat_name="Bb3";note.freq=233.08f;
        notes.add(note);
        // Note B3
        note = new Note();
        note.id=24;note.name="B3";note.sharp_name="B3";note.flat_name="B3";note.freq=246.94f;
        notes.add(note);
        // Note C4 MIDDLE C!
        note = new Note();
        note.id=25;note.name="C4";note.sharp_name="C4";note.flat_name="C4";note.freq=261.63f;
        notes.add(note);
        // Note C#4/Db4
        note = new Note();
        note.id=26;note.name="C#4/Db4";note.sharp_name="C#4";note.flat_name="Db4";note.freq=277.18f;
        notes.add(note);
        // Note D4
        note = new Note();
        note.id=27;note.name="D4";note.sharp_name="D4";note.flat_name="D4";note.freq=293.66f;
        notes.add(note);
        // Note D#4/Eb4
        note = new Note();
        note.id=28;note.name="D#4/Eb4";note.sharp_name="D#4";note.flat_name="Eb4";note.freq=311.13f;
        notes.add(note);
        // Note E4
        note = new Note();
        note.id=29;note.name="E4";note.sharp_name="E4";note.flat_name="E4";note.freq=329.63f;
        notes.add(note);
        // Note F4
        note = new Note();
        note.id=30;note.name="F4";note.sharp_name="F4";note.flat_name="F4";note.freq=349.23f;
        notes.add(note);
        // Note F#4/Gb4
        note = new Note();
        note.id=31;note.name="F#4/Gb4";note.sharp_name="F#4";note.flat_name="Gb4";note.freq=369.99f;
        notes.add(note);
        // Note G4
        note = new Note();
        note.id=32;note.name="G4";note.sharp_name="G4";note.flat_name="G4";note.freq=392f;
        notes.add(note);
        // Note G#4/Ab4
        note = new Note();
        note.id=33;note.name="G#4/Ab4";note.sharp_name="G#4";note.flat_name="Ab4";note.freq=415.3f;
        notes.add(note);
        // Note A4
        note = new Note();
        note.id=34;note.name="A4";note.sharp_name="A4";note.flat_name="A4";note.freq=440f;
        notes.add(note);
        // Note A#4/Bb4
        note = new Note();
        note.id=35;note.name="A#4/Bb4";note.sharp_name="A#4";note.flat_name="Bb4";note.freq=466.16f;
        notes.add(note);
        // Note B4
        note = new Note();
        note.id=36;note.name="B4";note.sharp_name="B4";note.flat_name="B4";note.freq=493.88f;
        notes.add(note);
        // Note C5
        note = new Note();
        note.id=37;note.name="C5";note.sharp_name="C5";note.flat_name="C5";note.freq=523.25f;
        notes.add(note);
        // Note C#5/Db5
        note = new Note();
        note.id=38;note.name="C#5/Db5";note.sharp_name="C#5";note.flat_name="Db5";note.freq=554.37f;
        notes.add(note);
        // Note D5
        note = new Note();
        note.id=39;note.name="D5";note.sharp_name="D5";note.flat_name="D5";note.freq=587.33f;
        notes.add(note);
        // Note D#5/Eb5
        note = new Note();
        note.id=40;note.name="D#5/Eb5";note.sharp_name="D#5";note.flat_name="Eb5";note.freq=622.25f;
        notes.add(note);
        // Note E5
        note = new Note();
        note.id=41;note.name="E5";note.sharp_name="E5";note.flat_name="E5";note.freq=659.25f;
        notes.add(note);
        // Note F5
        note = new Note();
        note.id=42;note.name="F5";note.sharp_name="F5";note.flat_name="F5";note.freq=698.46f;
        notes.add(note);
        // Note F#5/Gb5
        note = new Note();
        note.id=43;note.name="F#5/Gb5";note.sharp_name="F#5";note.flat_name="Gb5";note.freq=739.99f;
        notes.add(note);
        // Note G5
        note = new Note();
        note.id=44;note.name="G5";note.sharp_name="G5";note.flat_name="G5";note.freq=783.99f;
        notes.add(note);
        // Note G#5/Ab5
        note = new Note();
        note.id=45;note.name="G#5/Ab5";note.sharp_name="G#5";note.flat_name="Ab5";note.freq=830.61f;
        notes.add(note);
        // Note A5
        note = new Note();
        note.id=46;note.name="A5";note.sharp_name="A5";note.flat_name="A5";note.freq=880f;
        notes.add(note);
    }

    /** Note ID from 1 to MAX_NOTE_ID based on frequency, returning
     * a linear interpolation between the Note ID of the nearest whole
     * note below freq and the nearest whole note above freq */
    public float frequency_to_note_id(float freq) {
        if (freq < MIN_FREQ || freq > MAX_FREQ) {
            return -1f;
        } else {
            for (int i = 0; i < notes.size() - 1; i++) {
                float low_f = notes.get(i).freq;
                float hi_f = notes.get(i + 1).freq;
                // Log.i("Sing", "low: " + low_f + " high: " + hi_f + " freq: " + freq);
                if (freq >= low_f  && freq < hi_f) {
                    // Fraction of distance between low_f and hi_f
                    float interpolate = (freq - low_f) / (hi_f - low_f);
                    return notes.get(i).id + interpolate;
                }
            }
        }
        return -1f;
    }

    /** Fraction of 0 to (MAX_NOTE_ID - 1) represented by
     * freq, using linear interpolation between whole note values */
    public float frequency_to_fraction(float freq) {
        return (frequency_to_note_id(freq) - 1f) / (MAX_NOTE_ID - 1f);
    }
}
