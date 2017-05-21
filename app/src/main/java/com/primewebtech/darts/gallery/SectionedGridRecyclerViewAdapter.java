package com.primewebtech.darts.gallery;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by benebsworth on 7/5/17.
 */


public class SectionedGridRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final String TAG = SectionedGridRecyclerViewAdapter.class.getSimpleName();
        private final Context mContext;
        private static final int SECTION_TYPE = 0;

        private boolean mValid = true;
        private int mSectionResourceId;
        private int mTextResourceId;
        private LayoutInflater mLayoutInflater;
        private RecyclerView.Adapter mBaseAdapter;
        private SparseArray<Section> mSections = new SparseArray<Section>();
        private RecyclerView mRecyclerView;


        public SectionedGridRecyclerViewAdapter(Context context, int sectionResourceId, int textResourceId,RecyclerView recyclerView,
                                                RecyclerView.Adapter baseAdapter) {
            // Initialise the sectionResourceId and the sectioncontent for a new sectionAdapter
            //

            mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mSectionResourceId = sectionResourceId;
            mTextResourceId = textResourceId;
            mBaseAdapter = baseAdapter;
            mContext = context;
            mRecyclerView = recyclerView;

            mBaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    mValid = mBaseAdapter.getItemCount()>0;
                    notifyDataSetChanged();
                }

                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    mValid = mBaseAdapter.getItemCount()>0;
                    notifyItemRangeChanged(positionStart, itemCount);
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    mValid = mBaseAdapter.getItemCount()>0;
                    notifyItemRangeInserted(positionStart, itemCount);
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    mValid = mBaseAdapter.getItemCount()>0;
                    notifyItemRangeRemoved(positionStart, itemCount);
                }
            });

            final GridLayoutManager layoutManager = (GridLayoutManager)(mRecyclerView.getLayoutManager());
            layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return (isSectionHeaderPosition(position)) ? layoutManager.getSpanCount() : 1 ;
                }
            });
        }


        public static class SectionViewHolder extends RecyclerView.ViewHolder {
            // ViewHolder. Captures variables for hydration of view when called

            public TextView title;

            public SectionViewHolder(View view, int mTextResourceid) {
                super(view);
                title = (TextView) view.findViewById(mTextResourceid);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int typeView) {
            if (typeView == SECTION_TYPE) {
                final View view = LayoutInflater.from(mContext).inflate(mSectionResourceId, parent, false);
                return new SectionViewHolder(view,mTextResourceId);
            }else{
                return mBaseAdapter.onCreateViewHolder(parent, typeView -1);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder sectionViewHolder, int position) {
            // carry out the hydration of the view when called.
            if (isSectionHeaderPosition(position)) {
                Log.d(TAG, "onBindViewHolder:Header");
                ((SectionViewHolder)sectionViewHolder).title.setText(mSections.get(position).title);
            }else{
                Log.d(TAG, "onBindViewHolder:Item");
//                mBaseAdapter.onBindViewHolder(sectionViewHolder,sectionedPositionToPosition(position));
                mBaseAdapter.onBindViewHolder(sectionViewHolder,sectionedPositionToPosition(position));
            }

        }

        @Override
        public int getItemViewType(int position) {
            // get the ViewType for the item at given position. Will check if it is a header
            // position else
            return isSectionHeaderPosition(position)
                    ? SECTION_TYPE
                    : mBaseAdapter.getItemViewType(sectionedPositionToPosition(position)) + 1;
        }


        public static class Section {
            /*
            Class responding for holding the mapping for a section. Will use firstPosition to work
            out howmany items will be contained in this given section by comparing it to the next
            firstPosition of the neighboring section. It will then have items in it equal to:

            no_itemsc = next_section_firstPosition - this_sections_firstPostion

            And if there's no next section it will effectively default to an upper bound value of
            100. This results in the last section effectively being

            no_items = 100 - this_sections_firstPosition

            What is responsible for working out how this items get allocated?

             * setSections will carry out an initial sort which will order the sections according to
            the firstPosition.
             */
            int firstPosition;
            int sectionedPosition;
            CharSequence title;

            public Section(int firstPosition, CharSequence title) {
                this.firstPosition = firstPosition;
                this.title = title;
            }

            public CharSequence getTitle() {
                return title;
            }
        }


        public void setSections(Section[] sections) {
            mSections.clear();

            Arrays.sort(sections, new Comparator<Section>() {
                @Override
                public int compare(Section o, Section o1) {
                    return (o.firstPosition == o1.firstPosition)
                            ? 0
                            : ((o.firstPosition < o1.firstPosition) ? -1 : 1);
                }
            });

            int offset = 0; // offset positions for the headers we're adding
            for (Section section : sections) {
                section.sectionedPosition = section.firstPosition + offset;
                mSections.append(section.sectionedPosition, section);
                ++offset;
            }

            notifyDataSetChanged();
        }

        public int positionToSectionedPosition(int position) {
            int offset = 0;
            for (int i = 0; i < mSections.size(); i++) {
                if (mSections.valueAt(i).firstPosition > position) {
                    break;
                }
                ++offset;
            }
            return position + offset;
        }

        public int sectionedPositionToPosition(int sectionedPosition) {
            if (isSectionHeaderPosition(sectionedPosition)) {
                return RecyclerView.NO_POSITION;
            }

            int offset = 0;
            for (int i = 0; i < mSections.size(); i++) {
                if (mSections.valueAt(i).sectionedPosition > sectionedPosition) {
                    break;
                }
                --offset;
            }
            return sectionedPosition + offset;
        }

        public boolean isSectionHeaderPosition(int position) {
            return mSections.get(position) != null;
        }


        @Override
        public long getItemId(int position) {
            return isSectionHeaderPosition(position)
                    ? Integer.MAX_VALUE - mSections.indexOfKey(position)
                    : mBaseAdapter.getItemId(sectionedPositionToPosition(position));
        }

        @Override
        public int getItemCount() {
            return (mValid ? mBaseAdapter.getItemCount() + mSections.size() : 0);
        }




}
