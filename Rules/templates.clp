(deftemplate customer
  (slot customer-id)
  (multislot name)
  (multislot address)
  (slot phone)
  (multislot credit-card)
)

(deftemplate product
  (slot part-number)
  (multislot name)
  (slot category)
  (slot price)
)

(deftemplate order
  (slot order-number)
  (slot customer-id)
  (slot payment-method)
)

(deftemplate line-item
  (slot order-number)
  (slot part-number)
  (slot customer-id)
  (slot quantity (default 1)))

