package com.test.dontforget.UI.MainHomeFragment

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.WeekDayPosition
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import com.test.dontforget.DAO.TodoClass
import com.test.dontforget.MainActivity
import com.test.dontforget.MyApplication
import com.test.dontforget.R
import com.test.dontforget.Repository.TodoRepository
import com.test.dontforget.UI.TodoAddFragment.TodoAddActivity
import com.test.dontforget.Util.LoadingDialog
import com.test.dontforget.Util.ThemeUtil
import com.test.dontforget.databinding.CalendarDayLayoutBinding
import com.test.dontforget.databinding.FragmentMainHomeBinding
import com.test.dontforget.databinding.RowHomeCategoryBinding
import com.test.dontforget.databinding.RowHomeCategoryTabBinding
import com.test.dontforget.databinding.RowHomeMemoSearchBinding
import com.test.dontforget.databinding.RowHomeTodoBinding
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

class MainHomeFragment : Fragment() {

    lateinit var binding: FragmentMainHomeBinding
    lateinit var mainActivity: MainActivity
    lateinit var mainHomeViewModel: MainHomeViewModel
    lateinit var loadingDialog: LoadingDialog
    lateinit var categoryIdxList: List<Long>

    var selectedCategoryPosition = 0
    lateinit var memoList: List<TodoClass>

    private val today = LocalDate.now()
    private var selectedDate = today
    private var monthToWeek = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        mainActivity = activity as MainActivity
        binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        loadingDialog = LoadingDialog(requireContext())

        mainHomeViewModel = ViewModelProvider(this)[MainHomeViewModel::class.java]
        mainHomeViewModel.run {
            categories.observe(mainActivity) {
                binding.recyclerViewMainHomeFragmentCategory.adapter?.notifyDataSetChanged()
                binding.recyclerViewMainHomeFragmentTodo.adapter?.notifyDataSetChanged()
            }

            categories2.observe(mainActivity) {
                binding.recyclerViewMainHomeFragmentCategory.adapter?.notifyDataSetChanged()
                binding.recyclerViewMainHomeFragmentTodo.adapter?.notifyDataSetChanged()
            }

            todoList.observe(mainActivity) {
                binding.recyclerViewMainHomeFragmentCategory.adapter?.notifyDataSetChanged()
                binding.recyclerViewMainHomeFragmentTodo.adapter?.notifyDataSetChanged()
            }

            todoList2.observe(mainActivity) {
                memoList = mainHomeViewModel.getTodo()
                binding.recyclerViewMainHomeFragmentMemoSearch.adapter?.notifyDataSetChanged()
            }
        }

        categoryIdxList =
            mainHomeViewModel.getCategoryAll(MyApplication.loginedUserInfo.userIdx, loadingDialog)
        setTodoData()
        mainHomeViewModel.getTodo(
            categoryIdxList
        )

        binding.run {
            textInputEditTextMainHomeFragment.run {
                doOnTextChanged { text, start, before, count ->
                    val newText = text.toString()
                    memoList = mainHomeViewModel.todoList2.value?.filter {
                        it.todoContent.contains(newText)
                    }!!

                    binding.recyclerViewMainHomeFragmentMemoSearch.adapter?.notifyDataSetChanged()
                }

                onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        mainHomeViewModel.getTodo(
                            categoryIdxList
                        )
                        textInputLayoutMainHomeFragment.run {
                            endIconMode = TextInputLayout.END_ICON_CUSTOM
                            setEndIconDrawable(R.drawable.ic_close_24px)
                            setEndIconOnClickListener {
                                val inputMethodManager =
                                    mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                inputMethodManager.hideSoftInputFromWindow(
                                    textInputEditTextMainHomeFragment.windowToken, 0
                                )
                                textInputEditTextMainHomeFragment.text = null
                                textInputEditTextMainHomeFragment.clearFocus()
                            }
                        }
                        scrollViewMainHomeFragment.visibility = View.GONE
                        constraintLayoutMainHomeFragment.visibility = View.VISIBLE
                    } else {
                        mainHomeViewModel.getTodoByDate(
                            selectedDate.toString(), mainHomeViewModel.getCategoryAll(
                                MyApplication.loginedUserInfo.userIdx, loadingDialog
                            )
                        )
                        textInputLayoutMainHomeFragment.endIconMode = TextInputLayout.END_ICON_NONE
                        scrollViewMainHomeFragment.visibility = View.VISIBLE
                        constraintLayoutMainHomeFragment.visibility = View.GONE
                    }
                }
            }

            recyclerViewMainHomeFragmentCategory.run {
                adapter = CategoryTabRecyclerViewAdapter()
            }

            recyclerViewMainHomeFragmentTodo.run {
                adapter = CategoryRecyclerViewAdapter()
            }

            recyclerViewMainHomeFragmentMemoSearch.run {
                adapter = MemoSearchViewAdapter()
            }

            buttonMainHomeFragment.setOnClickListener {
//                mainActivity.replaceFragment(MainActivity.TODO_ADD_FRAGMENT, true, null)
                val todoAddintent = Intent(mainActivity, TodoAddActivity::class.java)
                startActivity(todoAddintent)
                mainActivity.finish()
            }

            val daysOfWeek = daysOfWeek()
            val titlesContainer = binding.titlesContainer.root
            titlesContainer.children.map { it as TextView }.forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }

            val currentMonth = YearMonth.now()
            val startMonth = currentMonth.minusMonths(100)
            val endMonth = currentMonth.plusMonths(100)
            setupMonthCalendar(startMonth, endMonth, currentMonth, daysOfWeek)
            setupWeekCalendar(startMonth, endMonth, currentMonth, daysOfWeek)

            binding.headerContainer.chipWeekMode.setOnClickListener {
                calendarWeekMode()
            }

            binding.headerContainer.headerGoToday.setOnClickListener {
                if (selectedDate != today) {
                    if (monthToWeek) {
                        calendarViewMainHomeFragment.scrollToDate(today)
                        dateClicked(today)
                    } else {
                        weekCalendarViewMainHomeFragment.scrollToDate(today)
                        dateClicked(today)
                    }
                } else {
                    if (monthToWeek) {
                        calendarViewMainHomeFragment.scrollToDate(today)
                    } else {
                        weekCalendarViewMainHomeFragment.scrollToDate(today)
                    }
                }
            }
        }


        return binding.root
    }

    private fun calendarWeekMode() {
        if (monthToWeek) {
            val targetDate = selectedDate ?: return
            binding.weekCalendarViewMainHomeFragment.scrollToWeek(targetDate)
            binding.headerContainer.chipWeekMode.text = "주"
            monthToWeek = false
        } else {
            val targetMonth =
                binding.weekCalendarViewMainHomeFragment.findLastVisibleDay()?.date?.yearMonth
                    ?: return
            binding.calendarViewMainHomeFragment.scrollToMonth(targetMonth)
            binding.headerContainer.chipWeekMode.text = "월"
            monthToWeek = true
        }

        val weekHeight = binding.weekCalendarViewMainHomeFragment.height
        val visibleMonthHeight =
            weekHeight * binding.calendarViewMainHomeFragment.findFirstVisibleMonth()?.weekDays.orEmpty()
                .count()

        val oldHeight = if (monthToWeek) weekHeight else visibleMonthHeight
        val newHeight = if (monthToWeek) visibleMonthHeight else weekHeight

        val animator = ValueAnimator.ofInt(oldHeight, newHeight)
        animator.addUpdateListener { anim ->
            binding.calendarViewMainHomeFragment.updateLayoutParams {
                height = anim.animatedValue as Int
            }
            binding.calendarViewMainHomeFragment.children.forEach { child ->
                child.requestLayout()
            }
        }

        animator.doOnStart {
            if (monthToWeek) {
                binding.calendarViewMainHomeFragment.isVisible = true
                binding.weekCalendarViewMainHomeFragment.isGone = true
            }
        }
        animator.doOnEnd {
            if (!monthToWeek) {
                binding.calendarViewMainHomeFragment.isGone = true
                binding.weekCalendarViewMainHomeFragment.isVisible = true
            } else {
                binding.calendarViewMainHomeFragment.updateLayoutParams {
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                }
            }
            updateTitle()
        }
        animator.duration = 250
        animator.start()
    }

    // 월 달력
    private fun setupMonthCalendar(
        startMonth: YearMonth,
        endMonth: YearMonth,
        currentMonth: YearMonth,
        daysOfWeek: List<DayOfWeek>,
    ) {
        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        dateClicked(date = day.date)
                    }
                }
            }
        }
        binding.calendarViewMainHomeFragment.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                bindDate(data.date, container.textView, data.position == DayPosition.MonthDate)
            }
        }
        binding.calendarViewMainHomeFragment.monthScrollListener = { updateTitle() }
        binding.calendarViewMainHomeFragment.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarViewMainHomeFragment.scrollToMonth(currentMonth)
    }

    // 주 달력
    private fun setupWeekCalendar(
        startMonth: YearMonth,
        endMonth: YearMonth,
        currentMonth: YearMonth,
        daysOfWeek: List<DayOfWeek>,
    ) {
        class WeekDayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: WeekDay
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText

            init {
                view.setOnClickListener {
                    if (day.position == WeekDayPosition.RangeDate) {
                        dateClicked(date = day.date)
                    }
                }
            }
        }
        binding.weekCalendarViewMainHomeFragment.dayBinder =
            object : WeekDayBinder<WeekDayViewContainer> {
                override fun create(view: View): WeekDayViewContainer = WeekDayViewContainer(view)
                override fun bind(container: WeekDayViewContainer, data: WeekDay) {
                    container.day = data
                    bindDate(
                        data.date, container.textView, data.position == WeekDayPosition.RangeDate
                    )
                }
            }
        binding.weekCalendarViewMainHomeFragment.weekScrollListener = { updateTitle() }
        binding.weekCalendarViewMainHomeFragment.setup(
            startMonth.atStartOfMonth(),
            endMonth.atEndOfMonth(),
            daysOfWeek.first(),
        )
        binding.weekCalendarViewMainHomeFragment.scrollToWeek(currentMonth.atStartOfMonth())
    }

    private fun bindDate(date: LocalDate, textView: TextView, isSelectable: Boolean) {
        textView.text = date.dayOfMonth.toString()
        if (isSelectable) {
            when {
                selectedDate == date -> {
                    textView.setBackgroundResource(R.drawable.background_select_date)
                }

                else -> {
                    textView.background = null
                }
            }
            // 일요일 텍스트 색상 설정
            if (date.dayOfWeek.value == 7) {
                textView.setTextColor(Color.RED)
            }
            // 토요일 텍스트 색상 설정
            if (date.dayOfWeek.value == 6) {
                textView.setTextColor(Color.BLUE)
            }
        } else {
            textView.setTextColor(Color.GRAY)
            textView.background = null
        }
    }

    private fun dateClicked(date: LocalDate) {
        Log.d("asdasdasd", date.toString())

        val currentSelection = selectedDate
        if (currentSelection == date) {
            //selectedDate = null
            binding.calendarViewMainHomeFragment.notifyDateChanged(currentSelection)
            binding.weekCalendarViewMainHomeFragment.notifyDateChanged(currentSelection)
        } else {
            selectedDate = date
            binding.calendarViewMainHomeFragment.notifyDateChanged(date)
            binding.weekCalendarViewMainHomeFragment.notifyDateChanged(date)
            if (currentSelection != null) {
                binding.calendarViewMainHomeFragment.notifyDateChanged(currentSelection)
                binding.weekCalendarViewMainHomeFragment.notifyDateChanged(currentSelection)
            }
        }
        selectedCategoryPosition = 0
        mainHomeViewModel.getTodoByDate(
            selectedDate.toString(), mainHomeViewModel.getCategoryAll(
                MyApplication.loginedUserInfo.userIdx, loadingDialog
            )
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateTitle() {
        val isMonthMode = binding.headerContainer.chipWeekMode.text == "월"
        if (isMonthMode) {
            val month =
                binding.calendarViewMainHomeFragment.findFirstVisibleMonth()?.yearMonth ?: return
            binding.headerContainer.headerYearTextView.text = "${month.year}년"
            binding.headerContainer.headerMonthTextView.text = "${month.month.value}월"
        } else {
            val week = binding.weekCalendarViewMainHomeFragment.findFirstVisibleWeek() ?: return
            val firstDate = week.days.first().date
            val lastDate = week.days.last().date
            if (firstDate.yearMonth == lastDate.yearMonth) {
                binding.headerContainer.headerYearTextView.text = "${firstDate.year}년"
                binding.headerContainer.headerMonthTextView.text = "${firstDate.month.value}월"
            } else {
                binding.headerContainer.headerMonthTextView.text =
                    "${firstDate.month.value} - ${lastDate.month.value}월"
                if (firstDate.year == lastDate.year) {
                    binding.headerContainer.headerYearTextView.text = "${firstDate.year}년"
                } else {
                    binding.headerContainer.headerYearTextView.text =
                        "${firstDate.month.value} - ${lastDate.month.value}월"
                }
            }
        }
    }

    private fun setTodoData() {
        selectedDate = today
        mainHomeViewModel.getTodoByDate(
            selectedDate.toString(),
            mainHomeViewModel.getCategoryAll(MyApplication.loginedUserInfo.userIdx, loadingDialog)
        )
    }

    private fun setTodoDataOtherDay() {
        mainHomeViewModel.getTodoByDate(
            selectedDate.toString(),
            mainHomeViewModel.getCategoryAll(MyApplication.loginedUserInfo.userIdx, loadingDialog)
        )
    }

    // 카테고리 탭
    inner class CategoryTabRecyclerViewAdapter :
        RecyclerView.Adapter<CategoryTabRecyclerViewAdapter.CategoryTabViewHolder>() {

        inner class CategoryTabViewHolder(private val binding: RowHomeCategoryTabBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val textViewCategoryName = binding.textViewRowCategoryTab
            val cardViewRowCategoryTab = binding.cardViewRowCategoryTab

            init {
                binding.root.setOnClickListener {
                    if (adapterPosition != 0) {
                        // 전체가 아닌 카테고리
                        mainHomeViewModel.getCategoryByCategoryIdx(
                            mainHomeViewModel.categories.value?.get(
                                adapterPosition - 1
                            )?.categoryIdx!!
                        )

                    } else {
                        // 전체 카테고리
                        mainHomeViewModel.getCategoryAll(
                            MyApplication.loginedUserInfo.userIdx, loadingDialog
                        )
                    }
                    val position = adapterPosition

                    // 이전에 선택한 항목의 배경색을 원래대로 돌려놓음
                    val previousSelectedPosition = selectedCategoryPosition
                    selectedCategoryPosition = position

                    // 클릭한 항목의 배경색을 변경하고 위치를 추적
                    notifyItemChanged(position)
                    notifyItemChanged(previousSelectedPosition)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryTabViewHolder =
            CategoryTabViewHolder(
                RowHomeCategoryTabBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

        override fun getItemCount(): Int {
            // 첫 번째 항목에 고정된 값을 추가하려면 +1을 해줍니다.
            return mainHomeViewModel.categories.value?.size!! + 1
        }

        override fun onBindViewHolder(holder: CategoryTabViewHolder, position: Int) {
            // 첫 번째 항목에 고정된 값을 설정
            if (position == 0) {
                holder.textViewCategoryName.text = "전체"

                // 포지션이 0이면서 선택된 상태인 경우 글자색을 흰색으로, 아닌 경우 검은색으로 설정
                val textColor = if (position == selectedCategoryPosition) {
                    Color.WHITE
                } else {
                    if (MyApplication.selectedTheme == ThemeUtil.DARK_MODE) {
                        Color.WHITE
                    } else {
                        Color.BLACK
                    }
                }
                holder.textViewCategoryName.setTextColor(textColor)

                val backgroundColor = if (position == selectedCategoryPosition) {
                    ContextCompat.getColor(holder.itemView.context, R.color.colorPrimary)
                } else {
                    ContextCompat.getColor(holder.itemView.context, R.color.transparent)
                }
                holder.cardViewRowCategoryTab.setCardBackgroundColor(backgroundColor)
            } else {
                // 데이터가 존재하는 경우에만 데이터를 설정
                mainHomeViewModel.categories.value?.let { categories ->
                    val dataIndex = position - 1 // 첫 번째 항목을 제외한 위치
                    holder.textViewCategoryName.text = categories[dataIndex].categoryName

                    // 포지션이 0이 아니면서 선택된 상태인 경우 글자색을 흰색으로, 아닌 경우 검은색으로 설정
                    val textColor = if (position == selectedCategoryPosition) {
                        categories[dataIndex].categoryFontColor.toInt()
                    } else {
                        if (MyApplication.selectedTheme == ThemeUtil.DARK_MODE) {
                            Color.WHITE
                        } else {
                            Color.BLACK
                        }
                    }
                    holder.textViewCategoryName.setTextColor(textColor)

                    val backgroundColor = if (position == selectedCategoryPosition) {
                        categories[dataIndex].categoryColor.toInt()
                    } else {
                        ContextCompat.getColor(holder.itemView.context, R.color.transparent)
                    }
                    holder.cardViewRowCategoryTab.setCardBackgroundColor(backgroundColor)
                }
            }
        }
    }

    // 카테고리
    inner class CategoryRecyclerViewAdapter :
        RecyclerView.Adapter<CategoryRecyclerViewAdapter.CategoryViewHolder>() {
        inner class CategoryViewHolder(private val binding: RowHomeCategoryBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val textViewCategory = binding.textViewRowHomeCategoryCategoryName
            val recyclerViewRowHomeCategory = binding.recyclerViewRowHomeCategory
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder =
            CategoryViewHolder(
                RowHomeCategoryBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

        override fun getItemCount(): Int = mainHomeViewModel.categories2.value?.size!!

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            holder.textViewCategory.run {
                val category = mainHomeViewModel.categories2.value?.get(position)
                text = category?.categoryName
                val color = category?.categoryColor?.toInt() ?: 0
                setTextColor(color)
            }
            holder.recyclerViewRowHomeCategory.run {
                val categoryIdx =
                    mainHomeViewModel.categories2.value?.get(position)?.categoryIdx ?: -1
                val todoListForCategory = mainHomeViewModel.getTodoListForCategory(categoryIdx)
                val isCategoryPublic =
                    mainHomeViewModel.categories2.value?.get(position)?.categoryIsPublic
                adapter = TodoRecyclerViewAdapter(todoListForCategory, isCategoryPublic!!)
                layoutManager = LinearLayoutManager(context)
            }
        }
    }

    // 할 일
    inner class TodoRecyclerViewAdapter(
        private val todoList: List<TodoClass>, private val isCategoryPublic: Long
    ) : RecyclerView.Adapter<TodoRecyclerViewAdapter.TodoViewHolder>() {

        inner class TodoViewHolder(private val binding: RowHomeTodoBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val checkBoxTodo = binding.checkBoxRowTodo
            val textViewTodo = binding.textViewRowTodo
            val textViewTodoMaker = binding.textViewRowTodoMaker
            val textViewRowTodoLocation = binding.textViewRowTodoLocation
            val cardViewLocation = binding.cardView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder =
            TodoViewHolder(
                RowHomeTodoBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

        override fun getItemCount(): Int = todoList.size

        override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
            val todo = todoList[position]
            holder.textViewTodo.run {
                text = todo.todoContent
                setOnClickListener {
                    val bundle = Bundle()
                    bundle.putLong("todoIdx", todo.todoIdx)
                    if (isCategoryPublic == 0L) {
                        mainActivity.replaceFragment(
                            MainActivity.TODO_DETAIL_PERSONAL_FRAGMENT, true, bundle
                        )
                    } else {
                        if (MyApplication.loginedUserInfo.userIdx == todo.todoOwnerIdx) {
                            mainActivity.replaceFragment(
                                MainActivity.TODO_DETAIL_PUBLIC_OWNER_FRAGMENT, true, bundle
                            )
                        } else {
                            mainActivity.replaceFragment(
                                MainActivity.TODO_DETAIL_PUBLIC_FRAGMENT, true, bundle
                            )
                        }
                    }
                }
            }
            holder.textViewTodoMaker.text = "by ${todo.todoOwnerName}"
            holder.textViewRowTodoLocation.text = if (todo.todoLocationName == "위치 없음") {
                todo.todoLocationName
            } else {
                val parts = todo.todoLocationName.split("@")
                if (parts.size > 1) {
                    parts[1]
                } else {
                    todo.todoLocationName
                }
            }

            if (todo.todoLocationName == "위치 없음") {
                holder.cardViewLocation.visibility = View.GONE
            }

            if (todo.todoIsChecked == 0L) {
                holder.checkBoxTodo.isChecked = false
                holder.textViewTodo.paintFlags =
                    holder.textViewTodo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                if (MyApplication.selectedTheme == ThemeUtil.DARK_MODE) {
                    holder.textViewTodo.setTextColor(resources.getColor(android.R.color.white))
                } else {
                    holder.textViewTodo.setTextColor(resources.getColor(android.R.color.black))
                }
            } else {
                holder.checkBoxTodo.isChecked = true
                holder.textViewTodo.paintFlags =
                    holder.textViewTodo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.textViewTodo.setTextColor(resources.getColor(R.color.accentGray))
            }

            holder.checkBoxTodo.setOnCheckedChangeListener { buttonView, isChecked ->
                val newTodoIsChecked: Long = if (isChecked) 1 else 0

                val todoDataClass = TodoClass(
                    todoIdx = todo.todoIdx,
                    todoContent = todo.todoContent,
                    todoIsChecked = newTodoIsChecked,
                    todoCategoryIdx = todo.todoCategoryIdx,
                    todoCategoryName = todo.todoCategoryName,
                    todoFontColor = todo.todoFontColor,
                    todoBackgroundColor = todo.todoBackgroundColor,
                    todoDate = todo.todoDate,
                    todoAlertTime = todo.todoAlertTime,
                    todoLocationName = todo.todoLocationName,
                    todoLocationLatitude = todo.todoLocationLatitude,
                    todoLocationLongitude = todo.todoLocationLongitude,
                    todoMemo = todo.todoMemo,
                    todoOwnerIdx = todo.todoOwnerIdx,
                    todoOwnerName = todo.todoOwnerName
                )

                TodoRepository.modifyTodo(todoDataClass) { task ->
                    memoList = mainHomeViewModel.getTodo()
                }

                if (isChecked) {
                    holder.textViewTodo.paintFlags =
                        holder.textViewTodo.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    holder.textViewTodo.setTextColor(resources.getColor(R.color.accentGray))
                } else {
                    holder.textViewTodo.paintFlags =
                        holder.textViewTodo.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    if (MyApplication.selectedTheme == ThemeUtil.DARK_MODE) {
                        holder.textViewTodo.setTextColor(resources.getColor(android.R.color.white))
                    } else {
                        holder.textViewTodo.setTextColor(resources.getColor(android.R.color.black))
                    }
                }
            }
        }
    }

    // 메모 검색
    inner class MemoSearchViewAdapter :
        RecyclerView.Adapter<MemoSearchViewAdapter.MemoSearchHolder>() {

        inner class MemoSearchHolder(private val binding: RowHomeMemoSearchBinding) :
            RecyclerView.ViewHolder(binding.root) {
            val textViewDate = binding.textViewRowMemoSearchDate
            val textViewCategory = binding.textViewRowMemoSearchCategory
            val checkBoxRowMemoSearch = binding.checkBoxRowMemoSearch
            val textViewRowMemoSearchMaker = binding.textViewRowMemoSearchMaker
            val textViewRowMemoSearch = binding.textViewRowMemoSearch
            val textViewLocation = binding.textViewRowMemoSearchLocation
            val cardViewLocation = binding.cardView
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoSearchHolder =
            MemoSearchHolder(
                RowHomeMemoSearchBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

        override fun getItemCount(): Int = memoList.size

        override fun onBindViewHolder(holder: MemoSearchHolder, position: Int) {
            val todo = memoList[position]
            val categories = mainHomeViewModel.getCategories()
            var isCategoryPublic: Long = 0
            for (i in categories) {
                if (todo.todoCategoryIdx == i.categoryIdx) {
                    isCategoryPublic = i.categoryIsPublic
                    holder.textViewCategory.setTextColor(i.categoryColor.toInt())
                }
            }

            holder.textViewDate.text = todo.todoDate
            holder.textViewCategory.text = todo.todoCategoryName
            holder.textViewRowMemoSearchMaker.text = "by ${todo.todoOwnerName}"
            holder.textViewLocation.text = if (todo.todoLocationName == "위치 없음") {
                todo.todoLocationName
            } else {
                val parts = todo.todoLocationName.split("@")
                if (parts.size > 1) {
                    parts[1]
                } else {
                    todo.todoLocationName
                }
            }

            if (todo.todoLocationName == "위치 없음") {
                holder.cardViewLocation.visibility = View.GONE
            }

            holder.textViewRowMemoSearch.run {
                text = todo.todoContent
                setOnClickListener {
                    val bundle = Bundle()
                    bundle.putLong("todoIdx", todo.todoIdx)
                    if (isCategoryPublic == 0L) {
                        mainActivity.replaceFragment(
                            MainActivity.TODO_DETAIL_PERSONAL_FRAGMENT, true, bundle
                        )
                    } else {
                        if (MyApplication.loginedUserInfo.userIdx == todo.todoOwnerIdx) {
                            mainActivity.replaceFragment(
                                MainActivity.TODO_DETAIL_PUBLIC_OWNER_FRAGMENT, true, bundle
                            )
                        } else {
                            mainActivity.replaceFragment(
                                MainActivity.TODO_DETAIL_PUBLIC_FRAGMENT, true, bundle
                            )
                        }
                    }
                }
            }

            holder.checkBoxRowMemoSearch.setOnCheckedChangeListener { buttonView, isChecked ->
                val newTodoIsChecked: Long = if (isChecked) 1 else 0

                val todoDataClass = TodoClass(
                    todoIdx = todo.todoIdx,
                    todoContent = todo.todoContent,
                    todoIsChecked = newTodoIsChecked,
                    todoCategoryIdx = todo.todoCategoryIdx,
                    todoCategoryName = todo.todoCategoryName,
                    todoFontColor = todo.todoFontColor,
                    todoBackgroundColor = todo.todoBackgroundColor,
                    todoDate = todo.todoDate,
                    todoAlertTime = todo.todoAlertTime,
                    todoLocationName = todo.todoLocationName,
                    todoLocationLatitude = todo.todoLocationLatitude,
                    todoLocationLongitude = todo.todoLocationLongitude,
                    todoMemo = todo.todoMemo,
                    todoOwnerIdx = todo.todoOwnerIdx,
                    todoOwnerName = todo.todoOwnerName
                )

                TodoRepository.modifyTodo(todoDataClass) { task ->
                }

                if (isChecked) {
                    holder.textViewRowMemoSearch.paintFlags =
                        holder.textViewRowMemoSearch.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    holder.textViewRowMemoSearch.setTextColor(resources.getColor(R.color.accentGray))
                } else {
                    holder.textViewRowMemoSearch.paintFlags =
                        holder.textViewRowMemoSearch.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    if (MyApplication.selectedTheme == ThemeUtil.DARK_MODE) {
                        holder.textViewRowMemoSearch.setTextColor(resources.getColor(android.R.color.white))
                    } else {
                        holder.textViewRowMemoSearch.setTextColor(resources.getColor(android.R.color.black))
                    }
                }
            }

            if (todo.todoIsChecked == 0L) {
                holder.checkBoxRowMemoSearch.isChecked = false
                holder.textViewRowMemoSearch.paintFlags =
                    holder.textViewRowMemoSearch.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                if (MyApplication.selectedTheme == ThemeUtil.DARK_MODE) {
                    holder.textViewRowMemoSearch.setTextColor(resources.getColor(android.R.color.white))
                } else {
                    holder.textViewRowMemoSearch.setTextColor(resources.getColor(android.R.color.black))
                }
            } else {
                holder.checkBoxRowMemoSearch.isChecked = true
                holder.textViewRowMemoSearch.paintFlags =
                    holder.textViewRowMemoSearch.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.textViewRowMemoSearch.setTextColor(resources.getColor(R.color.accentGray))
            }
        }
    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    override fun onResume() {
        super.onResume()

        FirebaseDatabase.getInstance().reference.child("categoryInfo")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    // 데이터를 다시 로드하고 어댑터에 설정
                    categoryIdxList = mainHomeViewModel.getCategoryAll(
                        MyApplication.loginedUserInfo.userIdx, loadingDialog
                    )
                    val currentDate = getCurrentDate()
                    if (currentDate == selectedDate.toString()) {
                        setTodoData()
                    } else {
                        setTodoDataOtherDay()
                    }
                    mainHomeViewModel.getTodo(categoryIdxList)
//                    setCalendar()
                }
            })

        FirebaseDatabase.getInstance().reference.child("todoInfo")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onDataChange(p0: DataSnapshot) {
                    // 데이터를 다시 로드하고 어댑터에 설정
                    categoryIdxList = mainHomeViewModel.getCategoryAll(
                        MyApplication.loginedUserInfo.userIdx, loadingDialog
                    )
                    val currentDate = getCurrentDate()
                    if (currentDate == selectedDate.toString()) {
                        setTodoData()
                    } else {
                        setTodoDataOtherDay()
                    }
                    mainHomeViewModel.getTodo(categoryIdxList)
//                    setCalendar()
                }
            })

        // 리사이클러뷰의 어댑터 초기화
        binding.recyclerViewMainHomeFragmentCategory.adapter = CategoryTabRecyclerViewAdapter()
        binding.recyclerViewMainHomeFragmentTodo.adapter = CategoryRecyclerViewAdapter()
        binding.recyclerViewMainHomeFragmentMemoSearch.adapter = MemoSearchViewAdapter()
    }
}