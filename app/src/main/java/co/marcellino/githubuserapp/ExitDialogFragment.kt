package co.marcellino.githubuserapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_exit_dialog.*

class ExitDialogFragment : DialogFragment() {
    private var exitDialogListener: OnExitDialogListener? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exit_dialog, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        this.exitDialogListener = activity as UserListActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_exit_positive.setOnClickListener {
            exitDialogListener?.onExitDialogActionChosen(true)
            dialog?.dismiss()
        }

        btn_exit_negative.setOnClickListener {
            exitDialogListener?.onExitDialogActionChosen(false)
            dialog?.dismiss()
        }
    }

    override fun onDetach() {
        super.onDetach()

        this.exitDialogListener = null
    }

    interface OnExitDialogListener {
        fun onExitDialogActionChosen(exit: Boolean)
    }
}
