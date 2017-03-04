package com.bonovo.mcuupdate_and_setting;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class RightFragmentCapabilities extends ListFragment {
    private Capabilities.CapabilitySet mSelected;

    private Spinner mSpinner;
    private CapabilitiesAdapter mCapsAdapter;
    private PresetsAdapter mPresetsAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSelected = Capabilities.getSelected(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.capabilities, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSpinner = (Spinner) view.findViewById(R.id.spinner_presets);
        List<Capabilities.CapabilitySet> presets = Arrays.asList(Capabilities.PRESET_NU_SERIES,
                Capabilities.PRESET_NR_SERIES,
                Capabilities.getCustom(getActivity()));

        mPresetsAdapter = new PresetsAdapter(getActivity(), presets);
        mSpinner.setAdapter(mPresetsAdapter);

        // Pre-select the saved item (the "Custom" item is dynamically created, so can't use indexOf())
        mSpinner.setSelection(mSelected.isEditable() ? presets.size() - 1
                : presets.indexOf(mSelected));

        // When the selection changes, we have to update the individual capabilities in the list
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelected = mPresetsAdapter.getItem(position);
                mCapsAdapter = new CapabilitiesAdapter(getActivity(), mSelected);
                setListAdapter(mCapsAdapter);
                mCapsAdapter.notifyDataSetChanged();
                Capabilities.setSelected(getActivity(), mSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mCapsAdapter = new CapabilitiesAdapter(getActivity(), mSelected);
        setListAdapter(mCapsAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (v.isEnabled() && mSelected != null && mSelected.isEditable()) {
            Capabilities.Capability cap = (Capabilities.Capability) l.getItemAtPosition(position);
            mSelected.set(cap, !mSelected.hasCapability(cap));

            l.setItemChecked(position, mSelected.hasCapability(cap));
            mCapsAdapter.notifyDataSetChanged();
        }
    }

    private static class PresetsAdapter extends BaseAdapter {
        private final LayoutInflater mInflater;
        private final Context mContext;
        private final List<Capabilities.CapabilitySet> mPresets;

        PresetsAdapter(Context context, List<Capabilities.CapabilitySet> presets) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mPresets = presets;
        }

        @Override
        public int getCount() {
            return mPresets.size();
        }

        @Override
        public Capabilities.CapabilitySet getItem(int position) {
            return mPresets.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) convertView;

            if (view == null) {
                view = (TextView) mInflater.inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            Capabilities.CapabilitySet item = getItem(position);
            view.setText(item.getTitle(mContext));
            return view;
        }
    }

    private static class CapabilitiesAdapter extends BaseAdapter {
        private final Context mContext;
        private final LayoutInflater mInflater;
        private final Capabilities.CapabilitySet mCaps;

        private static final Capabilities.Capability[] sValues = Capabilities.Capability.values();

        CapabilitiesAdapter(Context context, Capabilities.CapabilitySet selected) {
            mInflater = LayoutInflater.from(context);
            mContext = context;
            mCaps = selected;
        }

        @Override
        public int getCount() {
            return sValues.length;
        }

        @Override
        public Capabilities.Capability getItem(int position) {
            return sValues[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            CheckedTextView view = (CheckedTextView) convertView;

            if (view == null) {
                view = (CheckedTextView) mInflater.inflate(android.R.layout.simple_list_item_checked, parent, false);
                view.setEnabled(mCaps.isEditable());
            }

            Capabilities.Capability item = getItem(position);
            view.setText(item.getTitle(mContext));
            view.setChecked(mCaps.hasCapability(item));
            return view;
        }
    }
}
