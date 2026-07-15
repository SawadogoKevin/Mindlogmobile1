package com.mindforce.mindlog.ui.screens.materiels

import android.content.res.ColorStateList
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.mindforce.mindlog.R
import com.mindforce.mindlog.data.model.EtatMateriel
import com.mindforce.mindlog.data.model.PanneResponse
import com.mindforce.mindlog.data.model.StatutPanne
import com.mindforce.mindlog.data.repository.MaterielRepository
import com.mindforce.mindlog.data.repository.PanneRepository
import com.mindforce.mindlog.ui.theme.*

@Composable
fun MaterielDetailScreen(
    materielId: String,
    materielRepository: MaterielRepository,
    panneRepository: PanneRepository,
    onBack: () -> Unit,
    onSignalerPanne: (String) -> Unit
) {
    val viewModel: MaterielDetailViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return MaterielDetailViewModel(materielId, materielRepository, panneRepository) as T
        }
    })

    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    AndroidView(
        factory = { context ->
            val contextWrapper = ContextThemeWrapper(context, R.style.Theme_MindForce)
            val view = LayoutInflater.from(contextWrapper).inflate(R.layout.fragment_materiel_detail, null)
            val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
            toolbar?.setNavigationOnClickListener { onBack() }

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHistory)
            recyclerView?.layoutManager = LinearLayoutManager(context)
            recyclerView?.adapter = PanneHistoryAdapter()

            val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fabSignaler)
            fab?.setOnClickListener { onSignalerPanne(materielId) }

            view
        },
        update = { view ->
            val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
            val fab = view.findViewById<ExtendedFloatingActionButton>(R.id.fabSignaler)
            
            progressBar?.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            
            state.materiel?.let { materiel ->
                val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
                if (toolbar != null) {
                    toolbar.title = "${materiel.marque} ${materiel.modele}"
                }
                
                view.findViewById<TextView>(R.id.materielId)?.text = materiel.id
                
                // Rows info
                updateInfoRow(view.findViewById(R.id.rowType), "Type", materiel.typeMaterielNom ?: "-")
                updateInfoRow(view.findViewById(R.id.rowSerie), "N° série", materiel.numeroSerie ?: "-")
                updateInfoRow(view.findViewById(R.id.rowFournisseur), "Fournisseur", materiel.fournisseur ?: "-")
                updateInfoRow(view.findViewById(R.id.rowAcquisition), "Date d'acquisition", materiel.dateAcquisition ?: "-")

                val etatBadge = view.findViewById<TextView>(R.id.etatBadge)
                if (etatBadge != null) {
                    val (label, color) = when (materiel.etatActuel) {
                        EtatMateriel.BON -> "Bon" to StateBon
                        EtatMateriel.USAGE -> "Usagé" to StateUsage
                        EtatMateriel.EN_PANNE -> "En panne" to StateEnPanne
                        EtatMateriel.MAINTENANCE -> "Maintenance" to StateEnPanne
                        EtatMateriel.DECLASSE -> "Déclassé" to StateDeclasse
                        EtatMateriel.HORS_SERVICE -> "Hors service" to StateDeclasse
                        null -> "Inconnu" to android.graphics.Color.GRAY
                    }
                    val badgeColor = if (color is androidx.compose.ui.graphics.Color) {
                        val argb = (color.alpha * 255).toInt() shl 24 or 
                                   (color.red * 255).toInt() shl 16 or 
                                   (color.green * 255).toInt() shl 8 or 
                                   (color.blue * 255).toInt()
                        argb
                    } else {
                        color as Int
                    }
                    
                    etatBadge.text = label
                    etatBadge.setTextColor(badgeColor)
                    etatBadge.backgroundTintList = ColorStateList.valueOf(badgeColor).withAlpha(30)
                }

                fab?.visibility = if (materiel.etatActuel == EtatMateriel.BON || materiel.etatActuel == EtatMateriel.USAGE) View.VISIBLE else View.GONE
            }

            val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHistory)
            val emptyHistoryText = view.findViewById<TextView>(R.id.emptyHistoryText)
            
            if (state.historiquePannes.isEmpty() && !state.isLoading) {
                recyclerView?.visibility = View.GONE
                emptyHistoryText?.visibility = View.VISIBLE
            } else {
                recyclerView?.visibility = View.VISIBLE
                emptyHistoryText?.visibility = View.GONE
                (recyclerView?.adapter as? PanneHistoryAdapter)?.submitList(state.historiquePannes)
            }
        }
    )
}

private fun updateInfoRow(row: View?, label: String, value: String) {
    if (row == null) return
    row.findViewById<TextView>(R.id.label)?.text = label
    row.findViewById<TextView>(R.id.value)?.text = value
}

class PanneHistoryAdapter : RecyclerView.Adapter<PanneHistoryAdapter.ViewHolder>() {
    private var items = listOf<PanneResponse>()

    fun submitList(newItems: List<PanneResponse>) {
        if (items != newItems) {
            items = newItems
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_panne, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(panne: PanneResponse) {
            itemView.findViewById<TextView>(R.id.panneDate).text = panne.dateSignalement?.take(10) ?: "-"
            itemView.findViewById<TextView>(R.id.panneDescription).text = panne.descriptionPanne
            
            val statutBadge = itemView.findViewById<TextView>(R.id.statutBadge)
            if (statutBadge != null) {
                val (label, color) = when (panne.statutEtape) {
                    StatutPanne.SIGNALE -> "Signalée" to StateUsage
                    StatutPanne.EN_REPARATION -> "En réparation" to StateEnPanne
                    StatutPanne.RESOLUE -> "Résolue" to StateBon
                    StatutPanne.DECLASSE -> "Déclassé" to StateDeclasse
                }
                
                val badgeColor = (color.alpha * 255).toInt() shl 24 or 
                                 (color.red * 255).toInt() shl 16 or 
                                 (color.green * 255).toInt() shl 8 or 
                                 (color.blue * 255).toInt()

                statutBadge.text = label
                statutBadge.setTextColor(badgeColor)
                statutBadge.backgroundTintList = ColorStateList.valueOf(badgeColor).withAlpha(40)
            }
        }
    }
}
