package com.bry.donorhub.Fragments.Homepage

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bry.donorhub.Constants
import com.bry.donorhub.Fragments.Authentication.SignIn
import com.bry.donorhub.Model.Organisation
import com.bry.donorhub.R
import com.google.gson.Gson
import java.util.*


class PickOrganisation : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val ARG_PARAM1 = "param1"
    private val ARG_PARAM2 = "param2"
    private val ARG_ORGANSIATIONS = "ARG_ORGANSIATIONS"
    private lateinit var organisations: ArrayList<Organisation>
    private lateinit var listener: PickOrganisationInterface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            organisations = Gson().fromJson(it.getString(ARG_ORGANSIATIONS),
                Organisation.organisation_list::class.java).organisation_list
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is PickOrganisationInterface){
            listener = context
        }
    }

    var when_data_updated: (organisations: ArrayList<Organisation>) -> Unit = {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val va = inflater.inflate(R.layout.fragment_pick_organisation, container, false)
        val open_donations_relative: RelativeLayout = va.findViewById(R.id.open_donations_relative)
        val organisations_recyclerview: RecyclerView = va.findViewById(R.id.organisations_recyclerview)
        val swipeContainer: SwipeRefreshLayout = va.findViewById(R.id.swipeContainer)

        organisations_recyclerview.adapter = myOrganisationsListAdapter()
        organisations_recyclerview.layoutManager = LinearLayoutManager(context)

        swipeContainer.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                listener.whenReloadEverything()
            }
        })

        when_data_updated = {
            organisations = it
            organisations_recyclerview.adapter?.notifyDataSetChanged()
            swipeContainer.setRefreshing(false)
        }

        open_donations_relative.setOnClickListener {
            listener.whenPickOrganisationOpenMyDonations()
        }

        return va
    }

    internal inner class myOrganisationsListAdapter : RecyclerView.Adapter<ViewHolderOrganisations>() {

        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolderOrganisations {
            val vh = ViewHolderOrganisations(LayoutInflater.from(context)
                .inflate(R.layout.recycler_item_organisation, viewGroup, false))
            return vh
        }

        override fun onBindViewHolder(viewHolder: ViewHolderOrganisations, position: Int) {
            var organisation = organisations[position]

            viewHolder.org_name.text = organisation.name
            viewHolder.location_name.text = organisation.location_name

            viewHolder.pick_org.setOnClickListener {
                listener.whenPickOrganisationOrgPicked(organisation)
            }
        }

        override fun getItemCount() = organisations.size

    }

    internal inner class ViewHolderOrganisations (view: View) : RecyclerView.ViewHolder(view) {
        val org_name: TextView = view.findViewById(R.id.org_name)
        val pick_org: RelativeLayout = view.findViewById(R.id.pick_org)
        val location_name: TextView = view.findViewById(R.id.location_name)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String, organisations: String) =
            PickOrganisation().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_ORGANSIATIONS, organisations)
                }
            }
    }


    interface PickOrganisationInterface{
        fun whenPickOrganisationOrgPicked(organisation: Organisation)
        fun whenReloadEverything()
        fun whenPickOrganisationOpenMyDonations()
    }


}