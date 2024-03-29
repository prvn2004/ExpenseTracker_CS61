package com.college.splitiitp.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.college.splitiitp.Adapter.ExpensesAdapter
import com.college.splitiitp.DataFiles.ExpensesDataFile
import com.college.splitiitp.DataFiles.MainDataFile
import com.college.splitiitp.R
import com.college.splitiitp.databinding.FragmentGroupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.HashMap

class GroupFragment : Fragment() {
    var docid: String? = ""
    private lateinit var binding: FragmentGroupBinding
    lateinit var recyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var ExpensesLinkModel: ArrayList<ExpensesDataFile>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupBinding.inflate(inflater, container, false)
        val view = binding.root
        ExpensesLinkModel = arrayListOf()

        docid = requireArguments().getString("Docid")

        ImplementFun()

        return view
    }

    fun ImplementFun() {
        if (GroupFragment.isConnectionAvailable(requireActivity())) {
            getGrpName()
            getExpenses()
            getmyshare()
            changeFragment()
        } else {
            setupAnim()
        }
    }

    private fun setupAnim() {
        binding.animationView.setAnimation(R.raw.no_internet)
        binding.animationView.repeatCount = LottieDrawable.INFINITE
        binding.animationView.playAnimation()
    }

    private fun getExpenses() {
        val tsLong = System.currentTimeMillis()
        val ts = tsLong / 1000

        val currentuser = FirebaseAuth.getInstance().currentUser!!.uid.toString()

        val firestore = FirebaseFirestore.getInstance()
        val collectionReference =
            firestore.collection("Groups").document(docid.toString()).collection("Expenses").orderBy("timestamp", Query.Direction.DESCENDING)

        collectionReference.addSnapshotListener { value, error ->
            if (value == null || error != null) {
                Toast.makeText(activity, "Error fetching data", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }

            Log.d("DATA", value.toObjects(ExpensesDataFile::class.java).toString())
            ExpensesLinkModel.clear()
            ExpensesLinkModel.addAll(value.toObjects(ExpensesDataFile::class.java))

            recyclerView = binding.recyclerView
            recyclerView.setHasFixedSize(true)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = ExpensesAdapter(ExpensesLinkModel)

            recyclerView.visibility = View.VISIBLE
        }
    }

    fun getmyshare(){

        val tsLong = System.currentTimeMillis()
        val ts = tsLong / 1000


        val currentuser = FirebaseAuth.getInstance().currentUser!!.uid.toString()

        val firestore = FirebaseFirestore.getInstance()
        val collectionReference =
            firestore.collection("Groups").document(docid.toString())

        collectionReference.addSnapshotListener { value, error ->
            if (value == null || error != null) {
                Toast.makeText(activity, "Error fetching data", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
            Log.d("hey", value.toString())

            val Contribution: Any? = value.get("Contribution")
            val Share: Any? = value.get("Share")

            val mylist: MainDataFile = MainDataFile(Contribution = Contribution as HashMap<String, Long>?, Share = Share as HashMap<String, Long>?)

            val email = PreferenceManager.getDefaultSharedPreferences(this.activity).getString("Email", "");

            val sharemap= mylist.getShares()
            val contributionmap= mylist.getContributions()


//        Log.d("hello", sharemap.toString())
            val valueOfElement = sharemap?.get(email).toString()
            val valueOfElement2 = contributionmap?.get(email).toString()

            binding.shareText.text = "₹$valueOfElement"
            binding.contributionText.text =  "₹$valueOfElement2"

        }
    }

    fun getGrpName() {
        val firestore = FirebaseFirestore.getInstance()
        val email =
            PreferenceManager.getDefaultSharedPreferences(activity).getString("Email", "")

        val collectionReference =
            firestore.collection("Groups").document(docid.toString())
        collectionReference.addSnapshotListener { value, error ->
            val email =
                PreferenceManager.getDefaultSharedPreferences(activity).getString("Email", "")

            val grpName = value?.get("personMade").toString()
            val shares = value?.get(email.toString()).toString()
            binding.GrpName.text = grpName
            binding.contributionText.text = shares

            Log.d("hey2", value.toString())

              binding.shareText.text = shares

            if (value == null || error != null) {
                Toast.makeText(activity, "Error fetching data", Toast.LENGTH_SHORT).show()
                return@addSnapshotListener
            }
        }
    }

    fun changeFragment(){
        binding.addingexpense.setOnClickListener {
            val fram = activity?.supportFragmentManager?.beginTransaction()
            val frag: Fragment = AddexpenseFragment()
            val args = Bundle()
            args.putString("docid", docid)
            frag.setArguments(args)
            fram?.replace(R.id.main_container, frag)
            fram!!.addToBackStack("true")
            fram?.commit()
        }
    }



    companion object {
        fun isConnectionAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivityManager != null) {
                val netInfo = connectivityManager.activeNetworkInfo
                if (netInfo != null && netInfo.isConnected
                    && netInfo.isConnectedOrConnecting
                    && netInfo.isAvailable
                ) {
                    return true
                }
            }
            return false
        }
    }
}