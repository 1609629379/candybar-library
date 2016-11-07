package com.dm.material.dashboard.candybar.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dm.material.dashboard.candybar.R;
import com.dm.material.dashboard.candybar.items.FAQs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-present Dani Mahardhika
 *
 * Licensed under the Apache LicenseHelper, Version 2.0 (the "LicenseHelper");
 * you may not use this file except in compliance with the LicenseHelper.
 * You may obtain a copy of the LicenseHelper at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the LicenseHelper is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the LicenseHelper for the specific language governing permissions and
 * limitations under the LicenseHelper.
 */

public class FAQsAdapter extends RecyclerView.Adapter<FAQsAdapter.ViewHolder> {

    private Context mContext;
    private List<FAQs> mFAQs;
    private List<FAQs> mFAQsAll;

    public FAQsAdapter(@NonNull Context context, @NonNull List<FAQs> faqs) {
        mContext = context;
        mFAQs = faqs;
        mFAQsAll = new ArrayList<>();
        mFAQsAll.addAll(mFAQs);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_faqs_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.question.setText(mFAQs.get(position).getQuestion());
        holder.answer.setText(mFAQs.get(position).getAnswer());
    }

    @Override
    public int getItemCount() {
        return mFAQs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView question;
        TextView answer;

        ViewHolder(View itemView) {
            super(itemView);
            question = (TextView) itemView.findViewById(R.id.question);
            answer = (TextView) itemView.findViewById(R.id.answer);
        }
    }

    public void search(String query) {
        query = query.toLowerCase(Locale.getDefault());
        mFAQs.clear();
        if (query.length() == 0) mFAQs.addAll(mFAQsAll);
        else {
            for (FAQs faq : mFAQsAll) {
                String question = faq.getQuestion().toLowerCase(Locale.getDefault());
                String answer = faq.getAnswer().toLowerCase(Locale.getDefault());
                if (question.contains(query) || answer.contains(query)) {
                    mFAQs.add(faq);
                }
            }
        }
        notifyDataSetChanged();
    }

}
