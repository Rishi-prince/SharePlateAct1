package com.yourteam.shareplate.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.yourteam.shareplate.R
import com.yourteam.shareplate.ai.ShareBotService

class ShareBotFragment : Fragment(R.layout.fragment_share_bot) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etInput = view.findViewById<TextInputEditText>(R.id.etChatInput)
        val btnSend = view.findViewById<MaterialButton>(R.id.btnSendChat)
        val container = view.findViewById<LinearLayout>(R.id.layoutChatContainer)
        val scrollView = view.findViewById<ScrollView>(R.id.scrollViewChat)

        fun addMessage(text: String, isUser: Boolean) {
            val tv = TextView(requireContext()).apply {
                this.text = text
                textSize = 14f
                setPadding(32, 24, 32, 24)
                setTextColor(android.graphics.Color.parseColor(if (isUser) "#FFFFFF" else "#17324D"))
                background = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor(if (isUser) "#0F766E" else "#E6FFFA"))
                    cornerRadius = 24f
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(
                        if (isUser) 96 else 16,
                        12,
                        if (isUser) 16 else 96,
                        12
                    )
                    gravity = if (isUser) android.view.Gravity.END else android.view.Gravity.START
                }
            }
            container.addView(tv)
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
        }

        btnSend.setOnClickListener {
            val query = etInput.text.toString().trim()
            if (query.isEmpty()) return@setOnClickListener

            addMessage(query, true)
            etInput.setText("")

            val response = ShareBotService.ask(query)
            container.postDelayed({
                addMessage(response, false)
            }, 400)
        }
    }
}
