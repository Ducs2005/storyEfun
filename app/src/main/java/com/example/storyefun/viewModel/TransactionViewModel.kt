package com.example.storyefun.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyefun.data.models.Transactions
import com.example.storyefun.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val userID: String
) : ViewModel() {
    // StateFlow lưu danh sách giao dịch để hiển thị trong TransactionHistoryScreen
    private val _transactions = MutableStateFlow<List<Transactions>>(emptyList())
    val transactions: StateFlow<List<Transactions>> = _transactions

    // StateFlow lưu trạng thái giao dịch (thành công/lỗi) để hiển thị thông báo
    private val _transactionStatus = MutableStateFlow<String?>(null)
    val transactionStatus: StateFlow<String?> = _transactionStatus

    // Thêm giao dịch mới
    fun addTransaction(coin: Int, money: Double) {
        viewModelScope.launch {
            // Gọi repository để thêm giao dịch
            val result = repository.addTransaction(userID, coin, money)
            // Cập nhật trạng thái dựa trên kết quả
            _transactionStatus.value = when {
                result.isSuccess -> "Giao dịch thành công"
                else -> "Lỗi: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // Tải danh sách giao dịch
    fun loadTransactions() {
        viewModelScope.launch {
            // Gọi repository để lấy giao dịch
            val result = repository.getTransactions()
            // Cập nhật danh sách nếu thành công, hoặc thông báo lỗi
            if (result.isSuccess) {
                _transactions.value = result.getOrNull() ?: emptyList()
            } else {
                _transactionStatus.value = "Lỗi khi tải lịch sử: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    // Xóa thông báo trạng thái
    fun clearStatus() {
        _transactionStatus.value = null
    }
}