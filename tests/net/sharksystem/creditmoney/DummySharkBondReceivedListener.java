package net.sharksystem.creditmoney;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.ASAPStorage;
import net.sharksystem.pki.SharkPKIComponent;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class DummySharkBondReceivedListener implements SharkBondsReceivedListener {
    private final SharkCreditMoneyComponentImpl sharkCreditMoneyComponent;
    private SharkBond creditBond;

    public DummySharkBondReceivedListener(SharkCreditMoneyComponent sharkCreditMoneyComponent) {
        this.sharkCreditMoneyComponent = (SharkCreditMoneyComponentImpl) sharkCreditMoneyComponent;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          act on received bonds                                       //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void sharkBondReceived(CharSequence uri) throws IOException, SharkCreditMoneyException {
        CharSequence sender;
        Set<CharSequence> receiver;
        try {
            ASAPStorage asapStorage = this.sharkCreditMoneyComponent.getASAPStorage();
            byte[] asapMessage = asapStorage.getChannel(uri).getMessages(false).getMessage(0, true);
            SharkPKIComponent pkiStore = this.sharkCreditMoneyComponent.getSharkPKI();
            this.creditBond = SharkBondSerializer.deserializeCreditBond(asapMessage, pkiStore.getASAPKeyStore());
            switch(uri.toString()) {
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_CREDITOR_URI:
                    SharkBondHelper.signAsCreditor(pkiStore.getASAPKeyStore(), this.creditBond, false);
                    receiver = new HashSet<>();
                    receiver.add(creditBond.getDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(creditBond, creditBond.getCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_AS_DEBTOR_URI:
                    SharkBondHelper.signAsDebtor(pkiStore.getASAPKeyStore(), this.creditBond, false);
                    receiver = new HashSet<>();
                    receiver.add(creditBond.getCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(creditBond, creditBond.getDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI:
                    System.out.println("Step: 5, uri: " + SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    System.out.println("Current User: " + pkiStore.getASAPKeyStore().getOwner());
                    // Finalise exchange process and save sharkBond
                    this.sharkCreditMoneyComponent.saveBond(this.creditBond);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_ACCEPT_TRANSFER_DEBTOR_URI:
                    SharkBondHelper.acceptTransferDebtor(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ACCEPTED_TRANSFER_DEBTOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_ACCEPT_TRANSFER_CREDITOR_URI:
                    System.out.println("Step: 1, uri: " + SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_ACCEPT_TRANSFER_CREDITOR_URI);
                    System.out.println("Current User: " + pkiStore.getASAPKeyStore().getOwner());
                    SharkBondHelper.acceptTransferCreditor(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getCreditorID());
                    System.out.println("Receiver: " + this.creditBond.getCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ACCEPTED_TRANSFER_CREDITOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ACCEPTED_TRANSFER_DEBTOR_URI:
                    SharkBondHelper.acceptedTransferDebtor(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getTempDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_TRANSFER_BOND_AS_DEBTOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ACCEPTED_TRANSFER_CREDITOR_URI:
                    System.out.println("Step: 2, uri: " + SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ACCEPTED_TRANSFER_CREDITOR_URI);
                    System.out.println("Current User: " + pkiStore.getASAPKeyStore().getOwner());
                    SharkBondHelper.acceptedTransferCreditor(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getCreditorID());
                    System.out.println("Receiver: " + this.creditBond.getCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getTempCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_TRANSFER_BOND_AS_CREDITOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_TRANSFER_BOND_AS_DEBTOR_URI:
                    SharkBondHelper.signTransferBondAsDebtor(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_TRANSFER_BOND_AS_DEBTOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_TRANSFER_BOND_AS_CREDITOR_URI:
                    System.out.println("Step: 3, uri: " + SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ASKED_TO_SIGN_TRANSFER_BOND_AS_CREDITOR_URI);
                    System.out.println("Current User: " + pkiStore.getASAPKeyStore().getOwner());
                    SharkBondHelper.signTransferBondAsCreditor(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getDebtorID());
                    System.out.println("Receiver: " + this.creditBond.getDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_TRANSFER_BOND_AS_CREDITOR_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_TRANSFER_BOND_AS_DEBTOR_URI:
                    SharkBondHelper.signTransferBondAsCreditor(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getDebtorID());
                    receiver.add(this.creditBond.getTempDebtorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getCreditorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_TRANSFER_BOND_AS_CREDITOR_URI:
                    System.out.println("Step: 4, uri: " + SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_TRANSFER_BOND_AS_CREDITOR_URI);
                    System.out.println("Current User: " + pkiStore.getASAPKeyStore().getOwner());
                    SharkBondHelper.signTransferBondAsDebtor(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    receiver.add(this.creditBond.getCreditorID());
                    receiver.add(this.creditBond.getTempCreditorID());
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, this.creditBond.getDebtorID(), receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNUL_BOND_URI:
                    SharkBondHelper.annulBond(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    if (pkiStore.getASAPKeyStore().getOwner().equals(this.creditBond.getCreditorID())) {
                        sender = this.creditBond.getCreditorID();
                        receiver.add(this.creditBond.getDebtorID());
                    } else {
                        sender = this.creditBond.getDebtorID();
                        receiver.add(this.creditBond.getCreditorID());
                    }
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, sender, receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNULLED_BOND_URI);
                    break;
                case SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_ANNULLED_BOND_URI:
                    SharkBondHelper.annulBond(pkiStore.getASAPKeyStore(), this.creditBond);
                    receiver = new HashSet<>();
                    if (pkiStore.getASAPKeyStore().getOwner().equals(this.creditBond.getCreditorID())) {
                        sender = this.creditBond.getCreditorID();
                        receiver.add(this.creditBond.getDebtorID());
                    } else {
                        sender = this.creditBond.getDebtorID();
                        receiver.add(this.creditBond.getCreditorID());
                    }
                    this.sharkCreditMoneyComponent.sendBond(this.creditBond, sender, receiver, true, true, SharkCreditMoneyComponent.SHARK_CREDIT_MONEY_SIGNED_BOND_URI);
                    break;
                default:
                    throw new ASAPException("Shark Bond received on unknown uri");
            }

        } catch (ASAPException e) {
            System.out.println(e.getMessage());
        }
    }
}
