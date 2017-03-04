package com.bonovo.mcuupdate_and_setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class Capabilities {
    /**
     * Preset definition of capabilities for the NU series (Carpad II)
     */
    public static final CapabilitySet PRESET_NU_SERIES = new CapabilitySet(R.string.preset_nu_series,
            Arrays.asList(Capability.BACKLIGHT_KEYS_CUSTOM_COLOR),
            Arrays.asList(Capability.BACKLIGHT_KEYS_BRIGHTNESS));

    /**
     * Preset definition of capabilities for the NR series (Carpad III)
     */
    public static final CapabilitySet PRESET_NR_SERIES = new CapabilitySet(R.string.preset_nr_series,
            Arrays.asList(Capability.BACKLIGHT_KEYS_BRIGHTNESS),
            Arrays.asList(Capability.BACKLIGHT_KEYS_CUSTOM_COLOR));

    private static final String TAG = "Capabilities";
    private static final String PREFS_CUSTOM_CAPABILITIES = "com.bonovo.capabilities.custom";
    private static final String PREFS_CAPABILITIES = "com.bonovo.capabilities";
    private static final String PREFS_KEY_SELECTED = "selected";

    private static final String PREF_SELECTED_NU = "PRESET_NU_SERIES";
    private static final String PREF_SELECTED_NR = "PRESET_NR_SERIES";
    private static final String PREF_SELECTED_CUSTOM = "PRESET_CUSTOM";
    private static final String PREF_SELECTED_DEFAULT = PREF_SELECTED_NU;

    public static CapabilitySet getSelected(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_CAPABILITIES, Context.MODE_PRIVATE);
        String selected = prefs.getString(PREFS_KEY_SELECTED, PREF_SELECTED_DEFAULT);
        CapabilitySet set = getSelected(context, selected);

        if (set == null && !PREF_SELECTED_DEFAULT.equals(selected)) {
            Log.w(TAG, "Unexpected preset selection '" + selected + "', falling back to default: " + PREF_SELECTED_DEFAULT);
            // The previous selection may have been removed -- reset back to default
            set = getSelected(context, PREF_SELECTED_DEFAULT);
        }

        return set;
    }

    public static void setSelected(Context context, CapabilitySet capabilitySet) {
        if (capabilitySet == null) return;

        SharedPreferences prefs = context.getSharedPreferences(PREFS_CAPABILITIES, Context.MODE_PRIVATE);

        if (capabilitySet.isEditable()) {
            prefs.edit().putString(PREFS_KEY_SELECTED, PREF_SELECTED_CUSTOM).apply();
        } else if (capabilitySet == PRESET_NU_SERIES) {
            prefs.edit().putString(PREFS_KEY_SELECTED, PREF_SELECTED_NU).apply();
        } else if (capabilitySet == PRESET_NR_SERIES) {
            prefs.edit().putString(PREFS_KEY_SELECTED, PREF_SELECTED_NR).apply();
        }
    }

    private static CapabilitySet getSelected(Context context, String selected) {
        if (PREF_SELECTED_NU.equals(selected)) {
            return PRESET_NU_SERIES;
        } else if (PREF_SELECTED_NR.equals(selected)) {
            return PRESET_NR_SERIES;
        } else if (PREF_SELECTED_CUSTOM.equals(selected)) {
            return getCustom(context);
        }

        return null;
    }

    /**
     * Set of available capabilities
     */
    public enum Capability {
        /**
         * Allows setting a custom color for the backlit keys
         */
        BACKLIGHT_KEYS_CUSTOM_COLOR(R.string.cap_backlight_keys_custom_color),

        /**
         * Allows setting the brightness of the backlit keys
         */
        BACKLIGHT_KEYS_BRIGHTNESS(R.string.cap_backlight_keys_brightness),
        /* End */;

        public final int title;

        Capability(int title) {
            this.title = title;
        }

        /**
         * @return The title string for this Capability, or {@code null} if no title has been defined
         */
        public String getTitle(Context context) {
            return title == 0 ? null : context.getString(title);
        }
    }

    /**
     * Returns the user-customized set of capabilities.
     */
    static CapabilitySet getCustom(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_CUSTOM_CAPABILITIES, Context.MODE_PRIVATE);
        Collection<Capability> enabled = new ArrayList<Capability>();
        Collection<Capability> disabled = new ArrayList<Capability>();

        for (Map.Entry<String, ?> e : prefs.getAll().entrySet()) {
            if (e.getValue() instanceof Boolean) {
                try {
                    Capability cap = Capability.valueOf(e.getKey());

                    if (Boolean.TRUE.equals(e.getValue())) {
                        enabled.add(cap);
                    } else if (Boolean.FALSE.equals(e.getValue())) {
                        disabled.add(cap);
                    }
                } catch (IllegalArgumentException iae) {
                    Log.w(TAG, "Discarding unknown capability entry: " + e);
                }
            }
        }

        return new CapabilitySet(R.string.preset_custom, enabled, disabled, prefs);
    }

    /**
     * This represents a defined set of capabilities.
     * <p>
     * The pre-defined capability sets are unmodifiable, assigned by evaluating what each model is capable of.
     * <p>
     * It is also possible to create a customized set of capabilities, where the customizations are stored in shared preferences.
     */
    static class CapabilitySet {
        private final int mTitle;

        private final Collection<Capability> mEnabledCaps;
        private final Collection<Capability> mDisabledCaps;

        private SharedPreferences mPreferences;

        private final SharedPreferences.OnSharedPreferenceChangeListener mPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (mPreferences != null) {
                    Capability cap;

                    try {
                        cap = Capability.valueOf(key);
                    } catch (IllegalArgumentException iae) {
                        return;
                    }

                    set(cap, sharedPreferences.getBoolean(key, mEnabledCaps.contains(cap)));
                }
            }
        };

        /**
         * Creates a {@code CapabilitySet} with hard-coded capabilities. This object cannot be modified.
         */
        CapabilitySet(int title, Collection<Capability> enabled, Collection<Capability> disabled) {
            this(title, enabled, disabled, null);
        }

        /**
         * Creates a {@code CapabilitySet} with a {@link SharedPreferences}-backed storage component. These capabilities can be modified.
         *
         * @param title    String resource id to identify this {@code CapabilitySet}
         * @param enabled  The set of enabled {@link Capability capabilities}
         * @param disabled The set of disabled {@link Capability capabilities}
         * @param prefs    Optional SharedPreferences storage backing
         */
        CapabilitySet(int title, Collection<Capability> enabled, Collection<Capability> disabled, SharedPreferences prefs) {
            mTitle = title;
            mEnabledCaps = enabled;
            mDisabledCaps = disabled;
            mPreferences = prefs;

            if (prefs != null) {
                prefs.registerOnSharedPreferenceChangeListener(mPrefsListener);
            }
        }

        /**
         * @param cap The Capability to test
         * @return {@code true} if this {@code CapabilitySet} has enabled the given {@link Capability}; {@code false} otherwise
         */
        public boolean hasCapability(Capability cap) {
            return mEnabledCaps.contains(cap) && !mDisabledCaps.contains(cap);
        }

        /**
         * @param cap The Capability to test
         * @return {@code true} if this {@code CapabilitySet} has defined whether the {@code cap} Capability is enabled or disabled.
         */
        public boolean isDefined(Capability cap) {
            return mEnabledCaps.contains(cap) || mDisabledCaps.contains(cap);
        }

        /**
         * Attempts to set the Capability preference
         *
         * @param cap     The Capability to modify
         * @param enabled Whether the Capability should be enabled or not
         * @return {@code true} if a change was made; {@code false} if nothing was changed.
         * @throws UnsupportedOperationException if this CapabilitySet is incapable of being modified
         */
        public boolean set(Capability cap, boolean enabled) {
            boolean didChange;

            if (enabled) {
                didChange = enable(cap);
            } else {
                didChange = disable(cap);
            }

            if (didChange && mPreferences != null) {
                mPreferences.edit()
                        .putBoolean(cap.name(), enabled)
                        .apply();
            }

            return didChange;
        }

        /**
         * Disables the {@code cap} Capability
         *
         * @param cap The Capability to modify
         * @return {@code true} if a change was made; {@code false} if nothing was changed.
         * @see #set(Capability, boolean)
         */
        public boolean disable(Capability cap) {
            checkEditable();
            return mEnabledCaps.remove(cap) | mDisabledCaps.add(cap);
        }


        /**
         * Enables the {@code cap} Capability
         *
         * @param cap The Capability to modify
         * @return {@code true} if a change was made; {@code false} if nothing was changed.
         * @see #set(Capability, boolean)
         */
        public boolean enable(Capability cap) {
            checkEditable();
            return mDisabledCaps.remove(cap) | mEnabledCaps.add(cap);
        }

        /**
         * @return The title string for this CapabilitySet, or {@code null} if no title has been defined
         */
        public String getTitle(Context context) {
            return mTitle == 0 ? null : context.getString(mTitle);
        }

        /**
         * @return {@code true} if this CapabilitySet can be modified.
         * <p>
         * If this returns {@code false}, any calls to {@link #set(Capability, boolean)},
         * {@link #enable(Capability)}, or {@link #disable(Capability)} will throw {@link UnsupportedOperationException}.
         */
        public boolean isEditable() {
            return mPreferences != null;
        }

        private void checkEditable() {
            if (!isEditable()) throw new UnsupportedOperationException("This capability set is not editable");
        }
    }
}
