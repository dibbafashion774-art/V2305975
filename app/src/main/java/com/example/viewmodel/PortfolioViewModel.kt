package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.PortfolioSummary
import com.example.data.model.TransactionRecord
import com.example.data.model.WalletType
import com.example.data.repository.TradingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PortfolioViewModel(
    private val tradingRepository: TradingRepository
) : ViewModel() {

    val portfolioSummary: StateFlow<PortfolioSummary> = tradingRepository.portfolioSummary
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PortfolioSummary())

    val transactions: StateFlow<List<TransactionRecord>> = tradingRepository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedWallet = MutableStateFlow(WalletType.SPOT)
    val selectedWallet: StateFlow<WalletType> = _selectedWallet.asStateFlow()

    private val _dialogMessage = MutableStateFlow<String?>(null)
    val dialogMessage: StateFlow<String?> = _dialogMessage.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    fun setSelectedWallet(wallet: WalletType) {
        _selectedWallet.value = wallet
    }

    fun toggleBalanceVisibility() {
        viewModelScope.launch {
            tradingRepository.toggleBalanceVisibility()
        }
    }

    fun deposit(amount: Double, asset: String = "USDT", network: String = "TRC20") {
        viewModelScope.launch {
            tradingRepository.depositFunds(amount, asset, network)
                .onSuccess {
                    _isError.value = false
                    _dialogMessage.value = "Successfully deposited ${String.format("%.2f", amount)} $asset via $network"
                }
                .onFailure {
                    _isError.value = true
                    _dialogMessage.value = it.message ?: "Deposit failed"
                }
        }
    }

    fun withdraw(amount: Double, asset: String = "USDT", address: String, network: String = "TRC20") {
        viewModelScope.launch {
            tradingRepository.withdrawFunds(amount, asset, address, network)
                .onSuccess {
                    _isError.value = false
                    _dialogMessage.value = "Withdrawal request of ${String.format("%.2f", amount)} $asset submitted."
                }
                .onFailure {
                    _isError.value = true
                    _dialogMessage.value = it.message ?: "Withdrawal failed"
                }
        }
    }

    fun transfer(amount: Double, from: WalletType, to: WalletType) {
        viewModelScope.launch {
            tradingRepository.transferFunds(amount, from, to)
                .onSuccess {
                    _isError.value = false
                    _dialogMessage.value = "Transferred ${String.format("%.2f", amount)} USDT to ${to.name} Wallet"
                }
                .onFailure {
                    _isError.value = true
                    _dialogMessage.value = it.message ?: "Transfer failed"
                }
        }
    }

    fun resetSimulation() {
        viewModelScope.launch {
            tradingRepository.resetSimulation()
            _isError.value = false
            _dialogMessage.value = "Simulation balances reset to initial 7,000,000.00 USDT."
        }
    }

    fun dismissMessage() {
        _dialogMessage.value = null
    }
}
