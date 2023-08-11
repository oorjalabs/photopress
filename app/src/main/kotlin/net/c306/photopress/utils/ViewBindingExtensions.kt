package net.c306.photopress.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * The source for these functions are taken from here:
 * https://gist.github.com/gmk57/aefa53e9736d4d4fb2284596fb62710d
 */

/**
 * Activity binding delegate, may be used since onCreate up to onDestroy (inclusive)
 *
 * Example usage:
 *
 * ```kotlin
 * class ExampleActivity :  Activity() {
 *
 *     private val binding by viewBinding(LayoutViewBinding::inflate)
 *
 *     override fun onCreate(savedState: Bundle) {
 *         super.onCreate(savedState)
 *         setContentView(binding.root)
 *     }
 * }
 * ```
 */
inline fun <T : ViewBinding> FragmentActivity.viewBinding(
    crossinline factory: (LayoutInflater) -> T,
) =
    lazy(LazyThreadSafetyMode.NONE) {
        factory(layoutInflater)
    }

/**
 * Create a view binding for a Fragment.
 *
 * This allows us to not worry about clearing up resources in the correct lifecycle callbacks.
 * We can create our binding as if it were a `val` and not have to interact with it once it is created.
 *
 * Example usage:
 *
 * ```kotlin
 *
 * class UserCommentsFragments : Fragment(R.layout.fragment_user_comments) {
 *     private val binding by viewBinding(FragmentUserCommentsBinding::bind)
 * }
 * ```
 */
fun <T : ViewBinding> Fragment.viewBinding(factory: (View) -> T): ReadOnlyProperty<Fragment, T> =
    object : ReadOnlyProperty<Fragment, T>, DefaultLifecycleObserver {
        private var binding: T? = null
        
        override fun getValue(thisRef: Fragment, property: KProperty<*>): T =
            binding ?: factory(requireView()).also {
                // if binding is accessed after Lifecycle is DESTROYED, create new instance, but don't cache it
                if (viewLifecycleOwner.lifecycle.currentState.isAtLeast(
                        Lifecycle.State.INITIALIZED,
                    )
                ) {
                    viewLifecycleOwner.lifecycle.addObserver(this)
                    binding = it
                }
            }
        
        override fun onDestroy(owner: LifecycleOwner) {
            binding = null
        }
    }

/**
 * Not really a delegate, just a small helper for RecyclerView.ViewHolders
 */
inline fun <T : ViewBinding> ViewGroup.viewBinding(
    factory: (LayoutInflater, ViewGroup, Boolean) -> T,
) = factory(LayoutInflater.from(context), this, false)

/**
 * `DialogFragment` binding delegate, may be used since `onCreateView`/`onCreateDialog` up to
 * `onDestroy` (inclusive)
 */
inline fun <T : ViewBinding> DialogFragment.viewBinding(
    crossinline factory: (LayoutInflater) -> T,
) = lazy(LazyThreadSafetyMode.NONE) {
    factory(layoutInflater)
}