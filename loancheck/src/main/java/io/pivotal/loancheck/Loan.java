package io.pivotal.loancheck;


import java.util.Objects;

public class Loan {

  private String uuid, name, status;
  private long amount;

  public Loan() {
  }

  public Loan(String uuid, String name, long amount) {
    this.uuid = uuid;
    this.name = name;
    this.amount = amount;
    this.setStatus(Statuses.PENDING.name());
  }

  public String getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    if (status.equals(Statuses.APPROVED.name())
            || status.equals(Statuses.DECLINED.name())
            || status.equals(Statuses.PENDING.name())
            || status.equals(Statuses.REJECTED.name())) {
      this.status = status;
    } else {
      throw new IllegalArgumentException("Cannot set the LoanApplication's status to " + status);
    }
  }

  public long getAmount() {
    return amount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Loan that = (Loan) o;
    return amount == that.amount &&
            uuid.equals(that.uuid) &&
            name.equals(that.name) &&
            Objects.equals(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid, name, status, amount);
  }

  @Override
  public String toString() {
    return "Loan{" +
            "uuid='" + uuid + '\'' +
            ", name='" + name + '\'' +
            ", status='" + status + '\'' +
            ", amount=" + amount +
            '}';
  }
}
