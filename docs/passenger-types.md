# Passenger types and required details

The kiosk supports separate Regular, Student, Senior and PWD counts. The combined count cannot exceed available seats or ten passengers per booking. Regular passengers need no details and receive automatic passenger labels. Regular-only bookings continue straight to seats. Mixed bookings show one spacious form at a time for Student, Senior and PWD passengers, in the same order used for names, types and seats at payment.

No passenger names are requested; all passengers receive automatic booking labels. Back and Continue move through the passenger steps, with progress shown below the main heading. Continue validates the current passenger before advancing. Students also supply their student ID number; seniors supply a senior ID number; PWD passengers supply a PWD ID number and disability type. Empty or whitespace-only required fields block continuation. Payment rechecks completeness. Returning to counts preserves entries for retained passengers of the same type; Start Over clears the forms.

These checks establish completeness only, not identity or discount eligibility. The form reminds passengers to present valid ID at payment. Automatic passenger labels and individual passenger types use the existing booking storage. ID numbers and disability details remain in the current kiosk session and are not saved to the database. Fare calculations are unchanged.

Run PassengerTypesTest for mixed counts, required-field validation, ordered types, back navigation data and reset; PassengerCapacityTest checks the trip seat limits.



